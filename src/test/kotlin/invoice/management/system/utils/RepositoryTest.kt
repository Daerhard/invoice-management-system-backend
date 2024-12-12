package invoice.management.system.utils

import invoice.management.system.repositories.CardRepository
import invoice.management.system.repositories.CustomerRepository
import invoice.management.system.repositories.CardmarketOrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

open class RepositoryTest: MySQLTestContainer {

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var cardRepository: CardRepository

    @Autowired
    lateinit var cardmarketOrderRepository: CardmarketOrderRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    fun TestEntityManager.flushAndClear() {
        flush()
        clear()
    }
}
