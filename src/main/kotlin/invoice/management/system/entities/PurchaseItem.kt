package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "product")
data class PurchaseItem(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(name = "purchase_id")
    val purchaseId: Int,

    @Column(name = "count")
    val count: Int,

    @Column(name = "card_quality")
    val cardQuality: String,

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
