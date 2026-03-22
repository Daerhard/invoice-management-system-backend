package invoice.management.system.services.invoiceGeneration

import invoice.management.system.api.CustomersApiDelegate
import invoice.management.system.model.CustomerDto
import invoice.management.system.model.EmailUpdateDto
import invoice.management.system.repositories.CustomerRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository
): CustomersApiDelegate
 {

    override fun getAllCustomers(): ResponseEntity<List<CustomerDto>> {
        val customers = customerRepository.findAll()

        val customerDtos = customers.map { customer ->
            customer.toDto()
        }

        return ResponseEntity(customerDtos, HttpStatus.OK)
    }

    override fun getProfessionalCustomers(): ResponseEntity<List<CustomerDto>> {
        val customers = customerRepository.findByIsProfessional(true)
        return ResponseEntity(customers.map { it.toDto() }, HttpStatus.OK)
    }

    override fun updateCustomerEmail(userName: String, emailUpdateDto: EmailUpdateDto): ResponseEntity<CustomerDto> {
        val customer = customerRepository.findByUserName(userName)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        customer.email = emailUpdateDto.email
        val saved = customerRepository.save(customer)
        return ResponseEntity(saved.toDto(), HttpStatus.OK)
    }

}