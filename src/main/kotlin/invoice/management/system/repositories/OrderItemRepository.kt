package invoice.management.system.repositories

import invoice.management.system.entities.OrderItem
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository : CrudRepository<OrderItem, Int>
