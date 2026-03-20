package invoice.management.system.repositories

import invoice.management.system.entities.CardmarketOrder
import invoice.management.system.entities.Invoice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository : JpaRepository<Invoice, Long> {
    fun findByOrder(order: CardmarketOrder): Invoice?
}
