package de.tiendonam.prscraper.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import javax.persistence.*

@Entity
data class Commit (
        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
        var id: Long = 0,

        @Column(nullable = false, columnDefinition = "TEXT")
        var message: String = "",

        @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
        var pullRequest: PullRequest = PullRequest(),

        @Column(nullable = false)
        var hash: String = "",

        @Column(nullable = true)
        var hashParent: String? = null,

        @Column(nullable = false)
        var tree: String = "",

        @Column(nullable = false, columnDefinition = "TEXT")
        var author: String = "",

        @Column(nullable = false)
        var createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Repository
interface CommitRepo : CrudRepository<Commit, Long> {

        fun findByHashAndPullRequest(hash: String, pullRequest: PullRequest): List<Commit>

        @Query("SELECT c.id from Commit c")
        fun findAllIds(): List<Long>
}