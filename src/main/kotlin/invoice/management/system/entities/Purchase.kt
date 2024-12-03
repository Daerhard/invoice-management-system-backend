package invoice.management.system.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "purchase")
data class Purchase(

    @Id
    @Column(name = "id", nullable = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne
    val customer: Customer,

    @Column(name = "order_id")
    val orderId: Long,

    @Column(name = "date_of_payment")
    val dateOfPayment: LocalDateTime,

    @Column(name = "article_count")
    val articleCount: Int,

    @Column(name = "merchandise_value")
    val merchandiseValue: Double,

    @Column(name = "shipment_cost")
    val shipmentCost: Double,

    @Column(name = "total_value")
    val totalValue: Double,

    @Column(name = "commission")
    val commission: Double,

    @Column(name = "currency")
    val currency: String = "EUR",

    @OneToMany
    @JoinColumn(name = "purchase_id")
    val purchaseItems: List<PurchaseItem> = emptyList(),
)