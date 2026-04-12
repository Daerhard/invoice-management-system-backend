package invoice.management.system.services.refund

import invoice.management.system.api.NotFoundException
import invoice.management.system.api.RefundsApiDelegate
import invoice.management.system.model.RefundDto
import invoice.management.system.repositories.RefundRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import invoice.management.system.services.invoiceGeneration.mapper.toEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class RefundService(
    private val refundRepository: RefundRepository
) : RefundsApiDelegate {

    override fun createRefund(refundDto: RefundDto): ResponseEntity<RefundDto> {
        val entity = refundDto.toEntity()
        val saved = refundRepository.save(entity)
        return ResponseEntity(saved.toDto(), HttpStatus.CREATED)
    }

    override fun getAllRefunds(): ResponseEntity<List<RefundDto>> {
        val refunds = refundRepository.findAll()
        return ResponseEntity(refunds.map { it.toDto() }, HttpStatus.OK)
    }

    override fun deleteRefund(id: Int): ResponseEntity<Unit> {
        refundRepository.findById(id)
            .orElseThrow { NotFoundException("refund with id $id not found") }
        refundRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
