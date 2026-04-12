package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "supply_invoice_document")
data class SupplyInvoiceDocument(

    @Lob
    @Column(name = "pdf_data", nullable = false)
    val pdfData: ByteArray,

    @OneToOne
    @JoinColumn(name = "supply_id")
    var supply: Supply? = null

) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SupplyInvoiceDocument) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "SupplyInvoiceDocument(id=$id)"
}
