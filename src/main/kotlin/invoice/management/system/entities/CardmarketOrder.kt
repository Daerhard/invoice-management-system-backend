package invoice.management.system.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cardmarket_order")
data class CardmarketOrder(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @Column(name = "external_order_id")
    val externalOrderId: Long,

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

    @OneToMany(
        mappedBy = "cardmarketOrder",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val orderItems: MutableList<OrderItem> = mutableListOf(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardmarketOrder) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "CardmarketOrder(id=$id)"
    }
}