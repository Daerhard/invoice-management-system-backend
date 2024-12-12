package invoice.management.system.services.invoiceCreation

import invoice.management.system.api.CustomersApiDelegate
import invoice.management.system.model.CustomerDto
import invoice.management.system.repositories.CustomerRepository
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
            CustomerDto(
                userName = customer.userName,
                isProfessional = customer.isProfessional
            )
        }

        return ResponseEntity(customerDtos, HttpStatus.OK)
    }

}