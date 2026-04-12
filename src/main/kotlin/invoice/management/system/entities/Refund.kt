package invoice.management.system.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "refund")
data class Refund(

    @Column(name = "value", nullable = false, precision = 19, scale = 4)
    val value: BigDecimal,

    @Column(name = "year", nullable = false)
    val year: Int

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Refund) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Refund(id=$id)"
}
