package invoice.management.system.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "supply")
data class Supply(

    @Column(name = "value", nullable = false, precision = 19, scale = 4)
    val value: BigDecimal,

    @Column(name = "supply_date", nullable = false)
    val supplyDate: LocalDate,

    @Column(name = "product", nullable = false)
    val product: String,

    @OneToOne(mappedBy = "supply", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var document: SupplyInvoiceDocument? = null

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    fun attachDocument(document: SupplyInvoiceDocument) {
        this.document = document
        document.supply = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Supply) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Supply(id=$id)"
}
