package invoice.management.system.repositoryTests

import invoice.management.system.factories.EntityFactory.Companion.createCard
import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardRepositoryTest : RepositoryTest() {

    @Test
    fun whenFindByUserName_thenReturnEntity() {
        val expectedCard = createCard()
        cardRepository.save(expectedCard)
        entityManager.flushAndClear()

        val fetchedCard = cardRepository.findByIdOrNull(expectedCard.cardId)
        assertEquals(expectedCard.cardId, fetchedCard?.cardId)
        assertEquals(expectedCard.name, fetchedCard?.name)
        assertEquals(expectedCard.language, fetchedCard?.language)
        assertEquals(expectedCard.rarity, fetchedCard?.rarity)
        assertEquals(expectedCard.isFirstEdition, fetchedCard?.isFirstEdition)
        assertEquals(expectedCard.productId, fetchedCard?.productId)
    }
}