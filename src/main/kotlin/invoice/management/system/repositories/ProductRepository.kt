package invoice.management.system.repositories

import invoice.management.system.entities.PurchaseItem
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository: CrudRepository<PurchaseItem, Int>