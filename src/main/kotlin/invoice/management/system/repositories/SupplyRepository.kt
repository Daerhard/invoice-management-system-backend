package invoice.management.system.repositories

import invoice.management.system.entities.Supply
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SupplyRepository : JpaRepository<Supply, Int> {

    @Query("SELECT s FROM Supply s WHERE YEAR(s.supplyDate) = :year")
    fun findByYear(year: Int): List<Supply>
}
