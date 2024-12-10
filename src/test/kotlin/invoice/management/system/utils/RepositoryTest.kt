package invoice.management.system.utils

import invoice.management.system.repositories.CardRepository
import invoice.management.system.repositories.CustomerRepository
import invoice.management.system.repositories.OrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

open class RepositoryTest {

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var cardRepository: CardRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    fun TestEntityManager.flushAndClear() {
        flush()
        clear()
    }
}
