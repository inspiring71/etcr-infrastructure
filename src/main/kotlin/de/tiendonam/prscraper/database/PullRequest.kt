package de.tiendonam.prscraper.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import javax.persistence.*

enum class PRState {
    open, closed
}

@Entity
data class PullRequest (
        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
        var id: Long = 0,

        @Column(nullable = false)
        var ghNumber: Long = 0,

        @Column(nullable = false, columnDefinition = "TEXT")
        var title: String = "",

        @Column(nullable = false) @Enumerated(EnumType.STRING)
        var state: PRState = PRState.open,

        @Column(nullable = false, columnDefinition = "TEXT")
        var author: String = "",

        @Column(nullable = false)
        var createdAt: OffsetDateTime = OffsetDateTime.now()
)

@Repository
interface PullRequestRepo : CrudRepository<PullRequest, Long>