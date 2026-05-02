package invoice.management.system.repositories

import invoice.management.system.entities.PurchaseInvoiceItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PurchaseInvoiceItemRepository : JpaRepository<PurchaseInvoiceItem, Int> {

    @Query("SELECT i FROM PurchaseInvoiceItem i WHERE YEAR(i.invoiceDate) = :year")
    fun findByYear(year: Int): List<PurchaseInvoiceItem>
}
