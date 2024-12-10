package invoice.management.system.repositoryTests

import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest : RepositoryTest() {

    @Test
    fun whenFindByUserName_thenReturnEntity() {
        val expectedCustomer = createCustomer()
        customerRepository.save(expectedCustomer)
        entityManager.flushAndClear()

        val fetchedCustomer = customerRepository.findByUserName(expectedCustomer.userName)
        assertEquals(expectedCustomer.userName, fetchedCustomer?.userName)
        assertEquals(expectedCustomer.fullName, fetchedCustomer?.fullName)
        assertEquals(expectedCustomer.street, fetchedCustomer?.street)
        assertEquals(expectedCustomer.city, fetchedCustomer?.city)
        assertEquals(expectedCustomer.country, fetchedCustomer?.country)
        assertEquals(expectedCustomer.isProfessional, fetchedCustomer?.isProfessional)
        assertEquals(expectedCustomer.vatNumber, fetchedCustomer?.vatNumber)
    }
}