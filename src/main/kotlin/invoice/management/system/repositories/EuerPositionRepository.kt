package invoice.management.system.repositories

import invoice.management.system.entities.EuerPosition
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EuerPositionRepository : JpaRepository<EuerPosition, Long>
