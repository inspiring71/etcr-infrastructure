package de.tiendonam.prscraper.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.stream.Stream
import javax.persistence.*

enum class Topic {
        LGTM, // looks good to me
        PROBLEM_CODE_STYLE, // e.g. brackets, indentation, naming
        PROBLEM_TEST, // e.g. missing test
        PROBLEM_COMPLEXITY, // e.g unnecessary import, statement
        PROBLEM_STRUCTURE, // e.g. class hierarchy, method extraction
        PROBLEM_BUG, // e.g. wrong variable, code is not working
        PROBLEM_DOCUMENTATION, // e.g. comments, javadoc
        PROBLEM_TYPO,
        REQUEST_CHANGE, // need changes without further categorization
        QUESTION
}

@Entity
data class Comment (
        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
        var id: Long = 0,

        @Column(nullable = false, columnDefinition = "TEXT")
        var message: String = "",

        @Column(nullable = false)
        var ghId: Long = 0,

        @Column(nullable = true)
        var ghReplyId: Long? = null,

        @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
        var pullRequest: PullRequest = PullRequest(),

        @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
        var commit: Commit? = null,

        @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
        var commitFallback: Commit? = null,

        @Column(nullable = true, columnDefinition = "TEXT")
        var hunkDiff: String? = null,

        @Column(nullable = true, columnDefinition = "TEXT")
        var hunkFile: String? = null,

        @Column(nullable = false, columnDefinition = "TEXT")
        var author: String = "",

        @Column(nullable = false)
        var createdAt: OffsetDateTime = OffsetDateTime.now(),

        @Column(nullable = true) @Enumerated(EnumType.STRING)
        var classTopic: Topic? = null,

        @Column(nullable = true, columnDefinition = "TEXT")
        var note: String? = null,
)

@Repository
interface CommentRepo : CrudRepository<Comment, Long> {

        fun findByOrderById(): Stream<Comment>
        fun findByCommitOrCommitFallback(commit: Commit, commitFallback: Commit): List<Comment>
        fun findByClassTopicNotNull(): List<Comment>
}

@Service
class CommentStreamService (val commentRepo: CommentRepo) {

    @Transactional(readOnly = true)
    fun doLineByLine(action: (Comment) -> Unit) {
        commentRepo.findByOrderById().use { stream ->
            stream.forEach { comment ->
                action(comment)
            }
        }
    }
}