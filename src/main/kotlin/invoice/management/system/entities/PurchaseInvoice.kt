package invoice.management.system.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "purchase_invoice")
data class PurchaseInvoice(

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(name = "total_price", nullable = false, precision = 19, scale = 4)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @OneToMany(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val items: MutableList<PurchaseInvoiceItem> = mutableListOf()

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    fun addItem(item: PurchaseInvoiceItem) {
        items.add(item)
        item.invoice = this
        totalPrice = items.fold(BigDecimal.ZERO) { acc, i -> acc + i.price * i.amount.toBigDecimal() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PurchaseInvoice) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "PurchaseInvoice(id=$id)"
}

