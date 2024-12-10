package invoice.management.system.entities

import jakarta.persistence.*

@Entity
@Table(name = "card")
data class Card(

    @EmbeddedId
    val cardId: CardId,

    @Lob
    @Column(name = "complete_description")
    val completeDescription: String,

    @Column(name = "product_name")
    val productName: String,

    @Column(name = "name")
    val name: String,

    @Column(name = "language")
    val language: String,

    @Column(name = "rarity")
    val rarity: String,

    @Column(name = "product_id")
    val productId: Long,
    ) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Card) return false
        return cardId == other.cardId
    }

    override fun hashCode(): Int {
        return cardId.hashCode()
    }
}





