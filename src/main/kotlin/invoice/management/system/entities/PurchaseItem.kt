package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "purchase_item")
data class PurchaseItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val cardmarketPurchase: CardmarketPurchase,

    @Column(name = "count")
    val count: Int,

    @Column(name = "`condition`")
    val condition: String,

    @Column(name = "is_first_edition")
    val isFirstEdition: Boolean,

    @Column(name = "price")
    val price: Double,

    @ManyToOne
    @JoinColumns(
        value = [
            JoinColumn(name = "card_id_konami_set", referencedColumnName = "konami_set"),
            JoinColumn(name = "card_id_konami_number", referencedColumnName = "konami_number")
        ]
    )
    val card: Card,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PurchaseItem) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "PurchaseItem(id=$id)"
    }
}
