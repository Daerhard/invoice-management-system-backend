package invoice.management.system.services.euer

import invoice.management.system.api.EuerApiDelegate
import invoice.management.system.api.NotFoundException
import invoice.management.system.entities.EuerPosition
import invoice.management.system.entities.EuerReport
import invoice.management.system.entities.EuerSection
import invoice.management.system.model.CreateEuerRequestDto
import invoice.management.system.model.EuerPositionDto
import invoice.management.system.model.EuerPositionRequestDto
import invoice.management.system.model.EuerReportDto
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.EuerPositionRepository
import invoice.management.system.repositories.EuerReportRepository
import invoice.management.system.repositories.PurchaseInvoiceItemRepository
import invoice.management.system.repositories.RefundRepository
import invoice.management.system.repositories.SupplyRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import kotlin.math.abs

@Service
class EuerService(
    private val euerReportRepository: EuerReportRepository,
    private val euerPositionRepository: EuerPositionRepository,
    private val cardmarketOrderRepository: CardmarketOrderRepository,
    private val refundRepository: RefundRepository,
    private val supplyRepository: SupplyRepository,
    private val purchaseInvoiceItemRepository: PurchaseInvoiceItemRepository
) : EuerApiDelegate {

    @Transactional
    override fun createEuer(createEuerRequestDto: CreateEuerRequestDto): ResponseEntity<EuerReportDto> {
        val year = createEuerRequestDto.year
        if (euerReportRepository.existsByYear(year)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "EÜR for year $year already exists")
        }

        val report = euerReportRepository.save(EuerReport(year = year))
        autoCalculatePositions(report, year)

        val saved = euerReportRepository.findById(report.id).orElseThrow()
        return ResponseEntity(saved.toDto(), HttpStatus.CREATED)
    }

    @Transactional(readOnly = true)
    override fun getAllEuer(): ResponseEntity<List<EuerReportDto>> {
        return ResponseEntity(euerReportRepository.findAll().map { it.toDto() }, HttpStatus.OK)
    }

    @Transactional(readOnly = true)
    override fun getEuerById(id: Long): ResponseEntity<EuerReportDto> {
        val report = euerReportRepository.findById(id)
            .orElseThrow { NotFoundException("EÜR with id $id not found") }
        return ResponseEntity(report.toDto(), HttpStatus.OK)
    }

    @Transactional
    override fun deleteEuer(id: Long): ResponseEntity<Unit> {
        euerReportRepository.findById(id)
            .orElseThrow { NotFoundException("EÜR with id $id not found") }
        euerReportRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @Transactional
    override fun publishEuer(id: Long): ResponseEntity<EuerReportDto> {
        val report = euerReportRepository.findById(id)
            .orElseThrow { NotFoundException("EÜR with id $id not found") }
        report.published = true
        return ResponseEntity(euerReportRepository.save(report).toDto(), HttpStatus.OK)
    }

    @Transactional
    override fun addEuerPosition(id: Long, euerPositionRequestDto: EuerPositionRequestDto): ResponseEntity<EuerPositionDto> {
        val report = euerReportRepository.findById(id)
            .orElseThrow { NotFoundException("EÜR with id $id not found") }
        val position = EuerPosition(
            section = EuerSection.valueOf(euerPositionRequestDto.section.value),
            description = euerPositionRequestDto.description,
            value = euerPositionRequestDto.value.toBigDecimal(),
            automaticallyCalculated = false,
            euerReport = report
        )
        return ResponseEntity(euerPositionRepository.save(position).toDto(), HttpStatus.CREATED)
    }

    @Transactional
    override fun deleteEuerPosition(id: Long, positionId: Long): ResponseEntity<Unit> {
        euerReportRepository.findById(id)
            .orElseThrow { NotFoundException("EÜR with id $id not found") }
        euerPositionRepository.findById(positionId)
            .orElseThrow { NotFoundException("Position with id $positionId not found") }
        euerPositionRepository.deleteById(positionId)
        return ResponseEntity.noContent().build()
    }

    private fun autoCalculatePositions(report: EuerReport, year: Int) {
        val totalRevenue = cardmarketOrderRepository.sumTotalValueByYear(year) ?: 0.0
        if (totalRevenue != 0.0) {
            savePosition(report, EuerSection.BETRIEBSEINNAHMEN, "Umsatz $year", totalRevenue.toBigDecimal())
        }

        val purchaseCosts = purchaseInvoiceItemRepository.findByYear(year)
            .fold(BigDecimal.ZERO) { acc, item -> acc + item.price * item.amount.toBigDecimal() }
        if (purchaseCosts.compareTo(BigDecimal.ZERO) != 0) {
            savePosition(report, EuerSection.WAREN_ROHSTOFFE_HILFSSTOFFE, "Einkaufsrechnungen $year", purchaseCosts)
        }

        val refundTotal = refundRepository.findByYear(year)
            .fold(BigDecimal.ZERO) { acc, r -> acc + r.value }
        if (refundTotal.compareTo(BigDecimal.ZERO) != 0) {
            savePosition(report, EuerSection.WAREN_ROHSTOFFE_HILFSSTOFFE, "Erstattungen $year", refundTotal)
        }

        val supplyTotal = supplyRepository.findByYear(year)
            .fold(BigDecimal.ZERO) { acc, s -> acc + s.value }
        if (supplyTotal.compareTo(BigDecimal.ZERO) != 0) {
            savePosition(report, EuerSection.ARBEITSMITTEL, "Arbeitsmittel $year", supplyTotal)
        }

        val shipmentCosts = cardmarketOrderRepository.sumShipmentCostByYear(year) ?: 0.0
        if (shipmentCosts != 0.0) {
            savePosition(report, EuerSection.VERPACKUNG_TRANSPORT, "Versandkosten $year", shipmentCosts.toBigDecimal())
        }

        val commission = cardmarketOrderRepository.sumCommissionByYear(year) ?: 0.0
        if (commission != 0.0) {
            savePosition(report, EuerSection.UEBRIGE_BETRIEBSAUSGABEN, "Cardmarket Gebühr $year", abs(commission).toBigDecimal())
        }
    }

    private fun savePosition(report: EuerReport, section: EuerSection, description: String, value: BigDecimal) {
        euerPositionRepository.save(
            EuerPosition(
                section = section,
                description = description,
                value = value,
                automaticallyCalculated = true,
                euerReport = report
            )
        )
    }
}
