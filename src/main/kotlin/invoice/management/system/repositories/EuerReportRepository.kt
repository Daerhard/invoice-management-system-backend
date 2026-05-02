package invoice.management.system.repositories

import invoice.management.system.entities.EuerReport
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface EuerReportRepository : JpaRepository<EuerReport, Long> {

    fun findByYear(year: Int): EuerReport?

    fun existsByYear(year: Int): Boolean

    @EntityGraph(attributePaths = ["positions"])
    override fun findAll(): List<EuerReport>

    @EntityGraph(attributePaths = ["positions"])
    override fun findById(id: Long): Optional<EuerReport>
}
