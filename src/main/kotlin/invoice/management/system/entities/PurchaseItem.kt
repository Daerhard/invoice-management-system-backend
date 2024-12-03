package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "product")
data class PurchaseItem(

    @Id
    @Column(name = "id", nullable = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "purchase_id")
    val purchaseId: Long,

    @Column(name = "count")
    val count: Int,

    @Column(name = "condition")
    val condition: String,

    @Column(name = "price")
    val price: Double,

    @ManyToOne
    @JoinColumns(
        value = [
            JoinColumn(name = "card_id_konami_set", referencedColumnName = "konami_set", nullable = false, updatable = false, insertable = false),
            JoinColumn(name = "card_id_number", referencedColumnName = "number", nullable = false, updatable = false, insertable = false)
        ]
    )
    val card: Card,
)
