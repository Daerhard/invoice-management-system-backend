package invoice.management.system.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "purchase_invoice_item")
data class PurchaseInvoiceItem(

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_type", nullable = false)
    val purchaseType: PurchaseType,

    @Column(name = "amount", nullable = false)
    val amount: Int,

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    val price: BigDecimal,

    @Column(name = "invoice_date", nullable = false)
    val invoiceDate: LocalDate,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    var invoice: PurchaseInvoice? = null,

    @OneToOne(mappedBy = "item", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var document: PurchaseInvoiceDocument? = null

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    fun attachDocument(document: PurchaseInvoiceDocument) {
        this.document = document
        document.item = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PurchaseInvoiceItem) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "PurchaseInvoiceItem(id=$id)"
}
