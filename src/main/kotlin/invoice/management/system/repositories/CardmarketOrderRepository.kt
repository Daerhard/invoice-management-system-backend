package invoice.management.system.repositories

import invoice.management.system.entities.CardmarketOrder
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

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

    @EntityGraph(attributePaths = ["customer", "orderItems", "orderItems.card"])
    override fun findAll(): List<CardmarketOrder>

    @EntityGraph(attributePaths = ["customer", "orderItems", "orderItems.card"])
    fun findByCustomerUserName(userName: String): List<CardmarketOrder>

    @Query("SELECT COALESCE(SUM(o.totalValue), 0.0) FROM CardmarketOrder o WHERE YEAR(o.dateOfPayment) = :year")
    fun sumTotalValueByYear(year: Int): Double?

    @Query("SELECT COALESCE(SUM(o.shipmentCost), 0.0) FROM CardmarketOrder o WHERE YEAR(o.dateOfPayment) = :year")
    fun sumShipmentCostByYear(year: Int): Double?

    @Query("SELECT COALESCE(SUM(o.commission), 0.0) FROM CardmarketOrder o WHERE YEAR(o.dateOfPayment) = :year")
    fun sumCommissionByYear(year: Int): Double?

}
