package de.tiendonam.prscraper.scraping

import de.tiendonam.prscraper.database.PRState

/*
 * These classes represent the responses of the github API
 * Only relevant fields are implemented
 */

// pulls
data class PullRequestDto (
    val number: Long,
    val title: String,
    val state: PRState,
    val user: UserDto,
    val created_at: String
)

// commits
data class CommitDto (
    val sha: String,
    val commit: CommitRawDto,
    val parents: List<ParentDto>,
    val author: UserDto?
)

// comments
data class CommentDto (
    val body: String,
    val id: Long,
    val in_reply_to_id: Long?,
    val commit_id: String?,
    val original_commit_id: String?,
    val diff_hunk: String?,
    val path: String?,
    val user: UserDto,
    val created_at: String
)

// children
data class UserDto (val login: String?)
data class GitUserDto (val name: String, val email: String, val date: String)
data class GitTreeMetaDto (val sha: String)
data class CommitRawDto (val author: GitUserDto, val committer: GitUserDto, val message: String, val tree: GitTreeMetaDto)
data class ParentDto (val sha: String)