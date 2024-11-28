package invoice.management.system.entities

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "card")
data class Card(

    @EmbeddedId
    val id: CardId,

    @Lob
    @Column(name = "complete_description")
    val completeDescription: String,

    @Column(name = "product_name")
    val productName: String,

    @Column(name = "name")
    val name: String,

    @Column(name = "language")
    val language: String,

    @Column(name = "condition")
    val condition: String,

    @Column(name = "rarity")
    val rarity: String,

    @Column(name = "is_first_edition")
    val isFirstEdition: Boolean,

    @Column(name = "product_id")
    val productId: Long,

    )

@Embeddable
data class CardId(

    @Column(name = "konami_set")
    val konamiSet: String,

    @Column(name = "number")
    val number: Int
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardId) return false
        val that: CardId = other
        return konamiSet == that.konamiSet &&
                number == that.number
    }

    override fun hashCode(): Int {
        var result = konamiSet.hashCode()
        result = 31 * result + number
        return result
    }
}




