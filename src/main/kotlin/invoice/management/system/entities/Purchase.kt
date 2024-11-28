package invoice.management.system.entities

import jakarta.persistence.*
import java.sql.Date
import java.time.Instant

@Entity
@Table(name = "purchase")
data class Purchase(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(name = "customer_id")
    val customerId: Int,

    @Column(name = "order_id")
    val orderId: Long,

    @Column(name = "date_of_payment")
    val dateOfPayment: Date,

    @Column(name = "time_of_payment")
    val timeOfPayment: Instant,

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