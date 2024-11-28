package invoice.management.system.services

import invoice.management.system.entities.Card
import invoice.management.system.entities.Customer
import invoice.management.system.repositories.CardRepository
import invoice.management.system.repositories.CustomerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service


@Service
class DatabaseImportService(
    private val customerRepository: CustomerRepository,
    private val cardRepository: CardRepository,
) {

    fun saveCustomers(customers: List<Customer>) {
        customers.map { customer ->
            when(val existingCustomer = customerRepository.findByUserName(customer.userName)) {
                null -> customerRepository.save(customer)
                else -> {
                    existingCustomer.fullName = customer.fullName
                    existingCustomer.street = customer.street
                    existingCustomer.city = customer.city
                    existingCustomer.country = customer.country
                    existingCustomer.isProfessional = customer.isProfessional
                    customerRepository.save(existingCustomer)
                }
            }
        }
    }


    fun saveCards(cards: List<Card>) {
        cards.map { card ->
            cardRepository.findByIdOrNull(card.id) ?: cardRepository.save(card)
        }
    }

}