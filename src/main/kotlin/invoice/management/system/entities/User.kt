package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Column(name = "username", nullable = false, unique = true)
    val username: String,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(name = "zip_code", nullable = false)
    val zipCode: String,

    @Column(name = "city", nullable = false)
    val city: String,

    @Column(name = "street", nullable = false)
    val street: String,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0
}
