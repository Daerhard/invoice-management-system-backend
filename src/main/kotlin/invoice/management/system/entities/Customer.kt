package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "customer")
data class Customer(

    @Id
    @Column(name = "user_name")
    val userName: String,

    @Column(name = "full_name")
    var fullName: String,

    @Column(name = "street")
    var street: String,

    @Column(name = "city")
    var city: String,

    @Column(name = "country")
    var country: String,

    @Column(name = "is_professional")
    var isProfessional: Boolean,

    @Column(name = "vat_number", nullable = true)
    val vatNumber: String? = null,

    @OneToMany
    @JoinColumn(name = "customer_id")
    val orders: List<Purchase> = emptyList(),

    )
