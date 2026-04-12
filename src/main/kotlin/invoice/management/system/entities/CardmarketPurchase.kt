package invoice.management.system.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "cardmarket_purchase")
data class CardmarketPurchase(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "seller_user_name")
    val sellerUserName: String,

    @Column(name = "external_order_id")
    val externalOrderId: Long,

    @Column(name = "date_of_payment")
    val dateOfPayment: LocalDate,

    @Column(name = "article_count")
    val articleCount: Int,

    @Column(name = "merchandise_value")
    val merchandiseValue: Double,

    @Column(name = "shipment_cost")
    val shipmentCost: Double,

    @Column(name = "trustee_fee")
    val trusteeFee: Double,

    @Column(name = "total_value")
    val totalValue: Double,

    @Column(name = "currency")
    val currency: String = "EUR",

    @OneToMany(
        mappedBy = "cardmarketPurchase",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val purchaseItems: MutableList<PurchaseItem> = mutableListOf(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardmarketPurchase) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "CardmarketPurchase(id=$id)"
    }
}
