package invoice.management.system.repositories

import invoice.management.system.entities.Supply
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SupplyRepository : JpaRepository<Supply, Int>
