package de.tiendonam.prscraper.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

enum class StatusKey {
        STAGE
}

enum class StageValue {
        PULL_REQUESTS,
        COMMITS,
        COMMENTS,
        FILES,
        DONE
}

@Entity
data class ScrapingStatus (
        @Id
        var key: String = "",

        @Column(nullable = false)
        var value: String = ""
)

@Repository
interface ScrapingStatusRepo : CrudRepository<ScrapingStatus, String>