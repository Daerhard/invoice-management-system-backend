package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "order_item")
data class OrderItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val cardmarketOrder: CardmarketOrder,

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
            JoinColumn(name = "card_id_number", referencedColumnName = "number")
        ]
    )
    val card: Card,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "OrderItem(id=$id)"
    }
}
