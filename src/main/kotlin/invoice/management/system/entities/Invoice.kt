package invoice.management.system.entities

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "invoice")
data class Invoice(

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    val order: CardmarketOrder,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Lob
    @Column(name = "invoice_pdf")
    val invoicePdf: ByteArray? = null,

    @Column(name = "sent_at")
    val sentAt: LocalDate? = null,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Invoice) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Invoice(id=$id)"
}
