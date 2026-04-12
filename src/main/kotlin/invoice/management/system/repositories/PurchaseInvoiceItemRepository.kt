package invoice.management.system.repositories

import invoice.management.system.entities.PurchaseInvoiceItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseInvoiceItemRepository : JpaRepository<PurchaseInvoiceItem, Int>
