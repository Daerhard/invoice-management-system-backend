package invoice.management.system.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "purchase_invoice")
data class PurchaseInvoice(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(name = "amount", nullable = false)
    val amount: Int,

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    val price: BigDecimal,

    @Column(name = "invoice_date", nullable = false)
    val invoiceDate: LocalDate,

    @Lob
    @Column(name = "pdf_data", nullable = false)
    val pdfData: ByteArray,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PurchaseInvoice) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "PurchaseInvoice(id=$id)"
}
