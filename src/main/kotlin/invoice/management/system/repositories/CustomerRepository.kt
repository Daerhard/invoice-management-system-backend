package invoice.management.system.repositories

import invoice.management.system.entities.Customer
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository: CrudRepository<Customer, Int>{

    fun findByUserName(userName: String): Customer?
}