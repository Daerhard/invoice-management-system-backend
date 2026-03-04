package invoice.management.system.repositories

import invoice.management.system.entities.PurchaseInvoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseInvoiceRepository : JpaRepository<PurchaseInvoice, Int>
