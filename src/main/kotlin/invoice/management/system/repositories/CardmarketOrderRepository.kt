package invoice.management.system.repositories

import invoice.management.system.entities.CardmarketOrder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardmarketOrderRepository: CrudRepository<CardmarketOrder, Int> {

    fun findByExternalOrderId(externalOrderId: Long): CardmarketOrder?
}
