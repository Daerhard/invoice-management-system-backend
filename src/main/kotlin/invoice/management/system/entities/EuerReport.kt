package invoice.management.system.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "euer_report")
data class EuerReport(

    @Column(name = "year", nullable = false, unique = true)
    val year: Int,

    @Column(name = "published", nullable = false)
    var published: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDate = LocalDate.now(),

    @OneToMany(mappedBy = "euerReport", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val positions: MutableList<EuerPosition> = mutableListOf()

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EuerReport) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "EuerReport(id=$id, year=$year)"
}
