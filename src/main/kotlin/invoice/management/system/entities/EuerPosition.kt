package invoice.management.system.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "euer_position")
data class EuerPosition(

    @Enumerated(EnumType.STRING)
    @Column(name = "section", nullable = false)
    val section: EuerSection,

    @Column(name = "description", nullable = false)
    val description: String,

    @Column(name = "value", nullable = false, precision = 19, scale = 4)
    val value: BigDecimal,

    @Column(name = "automatically_calculated", nullable = false)
    val automaticallyCalculated: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "euer_report_id", nullable = false)
    var euerReport: EuerReport? = null

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EuerPosition) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "EuerPosition(id=$id)"
}
