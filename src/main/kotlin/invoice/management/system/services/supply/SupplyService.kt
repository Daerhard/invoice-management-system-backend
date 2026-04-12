package invoice.management.system.services.supply

import invoice.management.system.api.NotFoundException
import invoice.management.system.api.SuppliesApiDelegate
import invoice.management.system.entities.SupplyInvoiceDocument
import invoice.management.system.model.SupplyDto
import invoice.management.system.repositories.SupplyRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import invoice.management.system.services.invoiceGeneration.mapper.toEntity
import invoice.management.system.services.invoiceGeneration.mapper.toResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class SupplyService(
    private val supplyRepository: SupplyRepository
) : SuppliesApiDelegate {

    override fun createSupply(supplyData: SupplyDto, pdf: Resource?): ResponseEntity<SupplyDto> {
        val pdfBytes = try {
            pdf?.inputStream?.readBytes()
        } catch (ex: Exception) {
            return ResponseEntity.internalServerError().build()
        }

        if (pdfBytes != null && (pdfBytes.size < 4 ||
                    !pdfBytes.copyOfRange(0, 4)
                        .contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46)))
        ) {
            return ResponseEntity.badRequest().build()
        }

        val supply = supplyData.toEntity()

        if (pdfBytes != null) {
            val document = SupplyInvoiceDocument(pdfBytes)
            supply.attachDocument(document)
        }

        val saved = supplyRepository.save(supply)
        return ResponseEntity(saved.toDto(), HttpStatus.CREATED)
    }

    override fun getAllSupplies(): ResponseEntity<List<SupplyDto>> {
        val supplies = supplyRepository.findAll()
        return ResponseEntity(supplies.map { it.toDto() }, HttpStatus.OK)
    }

    override fun deleteSupply(id: Int): ResponseEntity<Unit> {
        supplyRepository.findById(id)
            .orElseThrow { NotFoundException("supply with id $id not found") }
        supplyRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    override fun getSupplyPdf(id: Int): ResponseEntity<Resource> {
        val supply = supplyRepository.findById(id)
            .orElseThrow { NotFoundException("supply with id $id not found") }

        val document = supply.document ?: throw NotFoundException("no document available for supply $id")

        return ResponseEntity(document.toResource(), HttpStatus.OK)
    }
}
