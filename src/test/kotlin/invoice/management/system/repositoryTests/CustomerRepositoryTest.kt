package invoice.management.system.repositoryTests

import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
        assertNull(fetchedCustomer?.email)
    }

    @Test
    fun whenFindByUserName_withEmail_thenReturnEntityWithEmail() {
        val expectedCustomer = createCustomer(email = "test@example.com")
        customerRepository.save(expectedCustomer)
        entityManager.flushAndClear()

        val fetchedCustomer = customerRepository.findByUserName(expectedCustomer.userName)
        assertEquals("test@example.com", fetchedCustomer?.email)
    }

    @Test
    fun whenFindByIsProfessional_thenReturnProfessionalCustomers() {
        val professionalCustomer = createCustomer(userName = "pro1", isProfessional = true)
        val nonProfessionalCustomer = createCustomer(userName = "nonpro1", isProfessional = false)
        customerRepository.save(professionalCustomer)
        customerRepository.save(nonProfessionalCustomer)
        entityManager.flushAndClear()

        val professionalCustomers = customerRepository.findByIsProfessional(true)
        assertEquals(1, professionalCustomers.size)
        assertEquals(professionalCustomer.userName, professionalCustomers[0].userName)
    }
}