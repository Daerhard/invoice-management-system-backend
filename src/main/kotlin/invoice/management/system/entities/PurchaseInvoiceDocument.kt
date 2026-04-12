package invoice.management.system.entities

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_invoice_document")
data class PurchaseInvoiceDocument(

    @Lob
    @Column(name = "pdf_data", nullable = false)
    val pdfData: ByteArray,

    @OneToOne
    @JoinColumn(name = "purchase_invoice_item_id")
    var item: PurchaseInvoiceItem? = null

    ) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PurchaseInvoiceDocument) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "PurchaseInvoiceDocument(id=$id)"

}
