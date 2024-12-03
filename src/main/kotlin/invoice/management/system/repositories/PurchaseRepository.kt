package invoice.management.system.repositories

import invoice.management.system.entities.Purchase
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseRepository: CrudRepository<Purchase, Int> {

    fun findByOrderId(orderId: Long): Purchase?
}
