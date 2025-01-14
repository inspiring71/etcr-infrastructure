package de.tiendonam.prscraper.scraping

import de.tiendonam.prscraper.database.*
import de.tiendonam.prscraper.utils.ExportRow
import de.tiendonam.prscraper.utils.ExportUtils
import de.tiendonam.prscraper.utils.RestClient
import de.tiendonam.prscraper.utils.parseJSON
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.time.OffsetDateTime
import javax.annotation.PostConstruct

@Service
class Scraper (
    private val restClient: RestClient,
    private val pullRequestRepo: PullRequestRepo,
    private val commitRepo: CommitRepo,
    private val commentRepo: CommentRepo,
    private val commentStreamService: CommentStreamService,
    private val gitFileRepo: GitFileRepo,
    private val scrapingStatusRepo: ScrapingStatusRepo,

    @Value("\${scraping.repository}")
    private val repository: String,

    @Value("\${scraping.export.classified}")
    private val exportClassified: Boolean,

    @Value("\${scraping.export.all}")
    private val exportAll: Boolean
) {

    private val logger = LoggerFactory.getLogger(Scraper::class.java)

    @PostConstruct
    fun run() {
        logger.info("Starting...")
        val status = scrapingStatusRepo.findByIdOrNull(StatusKey.STAGE.name)
        var stage = if (status != null) StageValue.valueOf(status.value) else setStage(StageValue.PULL_REQUESTS)

        if (stage == StageValue.PULL_REQUESTS) {
            logger.info("Stage (1/4): pull requests")
            fetchPRs()
            stage = setStage(StageValue.COMMITS)
        }

        if (stage == StageValue.COMMITS) {
            logger.info("Stage (2/4): commits")
            fetchCommits()
            stage = setStage(StageValue.COMMENTS)
        }

        if (stage == StageValue.COMMENTS) {
            logger.info("Stage (3/4): comments")
            fetchComments()
            stage = setStage(StageValue.FILES)
        }

        if (stage == StageValue.FILES) {
            logger.info("Stage (4/4): files")
            fetchFiles()
            setStage(StageValue.DONE)
        }

        if (exportClassified) {
            logger.info("Writing labeled results to csv...")

            // fetch rows
            val rows = commentRepo
                .findByClassTopicNotNull()
                .map { comment -> ExportRow(comment.id, comment.message, comment.classTopic?.name, comment.note, comment.author == comment.pullRequest.author) }

            ExportUtils.exportCSV(rows, "dataset_labeled.csv", preprocessing = false)
            ExportUtils.exportCSV(rows, "dataset_labeled_digested.csv", preprocessing = true)
            logger.info("Done.")
        }

        if (exportAll) {
            logger.info("Writing all results to csv...")

            // fetch rows (one by one)
            val rows = mutableListOf<ExportRow>()
            var counter = 0
            commentStreamService.doLineByLine { comment ->

                counter++
                if (counter % 10000 == 0) {
                    logger.info("Fetching all comments: $counter")
                }

                rows.add(ExportRow(comment.id, comment.message, comment.classTopic?.name, comment.note, comment.author == comment.pullRequest.author))
            }

            ExportUtils.exportCSV(rows, "dataset.csv", preprocessing = false)
            ExportUtils.exportCSV(rows, "dataset_digested.csv", preprocessing = true)
            logger.info("Done.")
        }
    }

    /**
     * fetch list of pull requests (meta data only)
     */
    fun fetchPRs(direction: String = "asc") {
        // fetch list of pull requests
        pullRequestRepo.deleteAll()
        var page = 1
        while (true) {
            val githubResponse :List<PullRequestDto>;
            try {
                githubResponse = restClient
                    .get("https://api.github.com/repos/$repository/pulls?state=all&sort=created&per_page=100&page=$page&direction=$direction")
                    ?.parseJSON<List<PullRequestDto>>()
                    ?: throw RuntimeException("Missing body in GET response. (fetching pulls)")
            } catch (e: Exception) {
                logger.info("Error at fetching PRs.")
                page++;
                if (page == 10000)
                    break;
                continue;
            }
            if (githubResponse.isEmpty())
                break // reached last page
            val prParsed :List<PullRequest>;
            try {
                prParsed = githubResponse.map { dto ->
                    PullRequest(
                        ghNumber = dto.number,
                        title = dto.title,
                        state = dto.state,
                        author = dto.user.login ?: "",
                        createdAt = OffsetDateTime.parse(dto.created_at)
                    )
                }
            } catch (e: Exception) {
                logger.info("Error at parsing PRs.")
                page++;
                if (page == 10000)
                    break;
                continue;
            }

            pullRequestRepo.saveAll(prParsed) // save to database
            logger.info("Fetched ${prParsed.size} PRs from page $page")
            page++
        }
    }

    /**
     * fetch commits for each pull request
     */
    fun fetchCommits() {
        commitRepo.deleteAll()
        val pullRequests = pullRequestRepo.findAll().toList()
        pullRequests.forEachIndexed { index, pullRequest ->
            val commitsDto = mutableListOf<CommitDto>()
            var page = 1
            while (true) {
                val githubResponse :List<CommitDto>;
                try {
                    githubResponse = restClient
                        .get("https://api.github.com/repos/$repository/pulls/${pullRequest.ghNumber}/commits?per_page=100&page=$page")
                        ?.parseJSON<List<CommitDto>>()
                        ?: throw RuntimeException("Missing body in GET response. (fetching commits)")
                } catch (e: Exception) {
                    logger.info("Error at fetching Commits.")
                    page++;
                    if (page == 10000)
                        break;
                    continue;
                }
                if (githubResponse.isEmpty())
                    break // reached page after last page

                commitsDto.addAll(githubResponse)
                page++

                if (githubResponse.size < 100)
                    break // reached last page
            }
            var add :Boolean;
            var commitsParsed :List<Commit>;
            try {
                commitsParsed = commitsDto.map { dto ->
                    Commit(
                        message = dto.commit.message,
                        pullRequest = pullRequest,
                        hash = dto.sha,
                        hashParent = dto.parents.firstOrNull()?.sha,
                        tree = dto.commit.tree.sha,
                        author = dto.author?.login ?: dto.commit.author.name,
                        createdAt = OffsetDateTime.parse(dto.commit.author.date)
                    )
                }
                add = true;
            } catch (e: Exception) {
                logger.info("Error at parsing Commits.")
                commitsParsed = emptyList();
                add = false;
            }
            if (add) {
                commitRepo.saveAll(commitsParsed)
                logger.info("Fetched ${commitsParsed.size} commits from PR #${pullRequest.ghNumber} (${index + 1} / ${pullRequests.size})")
            }
            Thread.sleep(500);
        }
    }

    /**
     * fetch comments for each pull request
     */
    fun fetchComments() {
        commentRepo.deleteAll()
        val pullRequests = pullRequestRepo.findAll().toList()
        pullRequests.forEachIndexed { index, pullRequest ->
            val commentsDtoA = mutableListOf<CommentDto>()
            val commentsDtoB = mutableListOf<CommentDto>()
            var page = 1
            while (true) {
                val githubResponse :List<CommentDto>;
                try {
                    githubResponse = restClient
                        .get("https://api.github.com/repos/$repository/pulls/${pullRequest.ghNumber}/comments?per_page=100&page=$page")
                        ?.parseJSON<List<CommentDto>>()
                        ?: throw RuntimeException("Missing body in GET response. (fetching comments via pull api)")
                } catch (e: Exception) {
                    logger.info("Error at fetching Comments.")
                    page++;
                    if (page == 10000)
                        break;
                    continue;
                }
                if (githubResponse.isEmpty())
                    break // reached page after last page

                commentsDtoA.addAll(githubResponse)
                page++

                if (githubResponse.size < 100)
                    break // reached last page
            }

            Thread.sleep(300)

            page = 1
            while (true) {
                val githubResponse :List<CommentDto>;
                try {
                    githubResponse = restClient
                        .get("https://api.github.com/repos/$repository/issues/${pullRequest.ghNumber}/comments?per_page=100&page=$page")
                        ?.parseJSON<List<CommentDto>>()
                        ?: throw RuntimeException("Missing body in GET response. (fetching comments via issue api)")
                } catch (e: Exception) {
                    logger.info("Error at parsing Comments.")
                    page++;
                    if (page == 10000)
                        break;
                    continue;
                }
                if (githubResponse.isEmpty())
                    break // reached page after last page

                commentsDtoB.addAll(githubResponse)
                page++

                if (githubResponse.size < 100)
                    break // reached last page
            }

            val commentsDto = commentsDtoA + commentsDtoB;
            var commentsParsed :List<Comment>;
            var add :Boolean;
            try {
                commentsParsed = commentsDto.map { dto ->
                    val commit = if (dto.original_commit_id != null) commitRepo.findByHashAndPullRequest(dto.original_commit_id, pullRequest) else null
                    val commitFallback = if (dto.commit_id != null) commitRepo.findByHashAndPullRequest(dto.commit_id, pullRequest) else null

                    Comment(
                        message = dto.body,
                        ghId = dto.id,
                        ghReplyId = dto.in_reply_to_id,
                        pullRequest = pullRequest,
                        commit = commit?.firstOrNull(),
                        commitFallback = commitFallback?.firstOrNull(),
                        hunkDiff = dto.diff_hunk,
                        hunkFile = dto.path,
                        author = dto.user.login ?: "",
                        createdAt = OffsetDateTime.parse(dto.created_at)
                    )
                }.sortedBy { comment -> comment.createdAt }
                add = true;
            } catch (e: Exception) {
                logger.info("Error at parsing Comments.")
                commentsParsed = emptyList();
                add = false;
            }
            if (add) {
                commentRepo.saveAll(commentsParsed)
                logger.info("Fetched ${commentsParsed.size} comments from PR #${pullRequest.ghNumber} (${index + 1} / ${pullRequests.size})")
            }
            Thread.sleep(500)
        }
    }

    /**
     * fetch files mentioned in each comment
     */
    fun fetchFiles() {
        gitFileRepo.deleteAll()
        val commits = commitRepo.findAllIds() // only fetch ids to avoid RAM issues
        commits.forEachIndexed { index, commitId ->

            // the actual commit
            val commit = commitRepo.findByIdOrNull(commitId) ?: throw RuntimeException("Commit with id $commitId not found")

            // list of file paths referred by comments for a specific commit
            val filePaths = commentRepo
                .findByCommitOrCommitFallback(commit, commit)
                .mapNotNull { comment -> comment.hunkFile }
                .distinct() // multiple comments may refer to same commit file

            val files = filePaths.mapNotNull { path ->
                val url = "https://raw.githubusercontent.com/$repository/${commit.hash}/$path"
                try {
                    val content = restClient.get(url)!!
                    // Thread.sleep(500)
                    GitFile(commit = commit, filePath = path, fileContent = content)
                } catch (e: Exception) {
                    // this is possible e.g. the file has been deleted
                    logger.error("Error fetching $url (PR #${commit.pullRequest.ghNumber})")
                    null
                }
            }

            gitFileRepo.saveAll(files)
            logger.info("Fetched ${files.size} files from commit ${commit.hash} (${index + 1} / ${commits.size})")
        }
    }

    /**
     * save stage to database and returns its value
     */
    private fun setStage(stage: StageValue): StageValue {
        scrapingStatusRepo.save(ScrapingStatus(StatusKey.STAGE.name, stage.name))
        return stage
    }
}