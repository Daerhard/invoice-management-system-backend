package invoice.management.system.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable


@Embeddable
class CardId(

    @Column(name = "konami_set")
    val konamiSet: String,

    @Column(name = "konami_number")
    val konamiNumber: String,

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CardId) return false
        return konamiSet== other.konamiSet && konamiNumber == other.konamiNumber
    }

    override fun hashCode(): Int {
        return konamiSet.hashCode() * 31 + konamiNumber.hashCode()
    }
}