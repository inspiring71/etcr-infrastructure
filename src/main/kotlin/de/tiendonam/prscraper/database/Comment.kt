package de.tiendonam.prscraper.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import javax.persistence.*

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
        var createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Repository
interface CommentRepo : CrudRepository<Comment, Long> {

        fun findByCommitOrCommitFallback(commit: Commit, commitFallback: Commit): List<Comment>
}