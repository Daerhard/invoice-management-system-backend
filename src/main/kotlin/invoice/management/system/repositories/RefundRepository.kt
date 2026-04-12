package invoice.management.system.repositories

import invoice.management.system.entities.Refund
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefundRepository : JpaRepository<Refund, Int>
