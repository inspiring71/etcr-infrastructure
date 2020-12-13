package de.tiendonam.prscraper.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
data class GitFile (
        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
        var id: Long = 0,

        @Column(nullable = false, columnDefinition = "TEXT")
        var filePath: String = "",

        @Column(nullable = false, columnDefinition = "TEXT")
        var fileContent: String = "",

        @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
        var commit: Commit = Commit()
)

@Repository
interface GitFileRepo : CrudRepository<GitFile, Long>