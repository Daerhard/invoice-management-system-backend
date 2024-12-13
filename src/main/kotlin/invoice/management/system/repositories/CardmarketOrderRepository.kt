package invoice.management.system.repositories

import invoice.management.system.entities.CardmarketOrder
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface CardmarketOrderRepository: CrudRepository<CardmarketOrder, Int> {

    fun findByExternalOrderId(externalOrderId: Long): CardmarketOrder?

    @Query(
        """
        select co from CardmarketOrder co
        where co.dateOfPayment >= :startDate 
        and co.dateOfPayment <= :endDate
    """
    )
    fun findByStartDateAndEndDate(startDate: LocalDate, endDate: LocalDate): List<CardmarketOrder>

}
