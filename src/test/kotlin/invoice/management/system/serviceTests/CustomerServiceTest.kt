package invoice.management.system.serviceTests

import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.model.EmailUpdateDto
import invoice.management.system.repositories.CustomerRepository
import invoice.management.system.services.invoiceGeneration.CustomerService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus

class CustomerServiceTest {

    private val customerRepository: CustomerRepository = mock(CustomerRepository::class.java)
    private val customerService = CustomerService(customerRepository)

    @Test
    fun whenGetProfessionalCustomers_thenReturnOnlyProfessionalCustomers() {
        val professionalCustomer = createCustomer(userName = "pro1", isProfessional = true)
        `when`(customerRepository.findByIsProfessional(true)).thenReturn(listOf(professionalCustomer))

        val response = customerService.getProfessionalCustomers()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.size)
        assertEquals("pro1", response.body?.first()?.userName)
    }

    @Test
    fun whenUpdateCustomerEmail_withExistingCustomer_thenReturnUpdatedCustomer() {
        val customer = createCustomer(userName = "testUser")
        val emailUpdateDto = EmailUpdateDto(email = "new@example.com")
        `when`(customerRepository.findByUserName("testUser")).thenReturn(customer)
        `when`(customerRepository.save(customer)).thenReturn(customer)

        val response = customerService.updateCustomerEmail("testUser", emailUpdateDto)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("new@example.com", response.body?.email)
    }

    @Test
    fun whenUpdateCustomerEmail_withNonExistingCustomer_thenReturn404() {
        `when`(customerRepository.findByUserName("unknown")).thenReturn(null)

        val response = customerService.updateCustomerEmail("unknown", EmailUpdateDto(email = "test@example.com"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun whenUpdateCustomerEmail_withNullEmail_thenClearEmail() {
        val customer = createCustomer(userName = "testUser", email = "old@example.com")
        val emailUpdateDto = EmailUpdateDto(email = null)
        `when`(customerRepository.findByUserName("testUser")).thenReturn(customer)
        `when`(customerRepository.save(customer)).thenReturn(customer)

        val response = customerService.updateCustomerEmail("testUser", emailUpdateDto)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(null, response.body?.email)
    }
}
