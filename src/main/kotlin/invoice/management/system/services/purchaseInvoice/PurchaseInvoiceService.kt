package invoice.management.system.services.purchaseInvoice

import invoice.management.system.api.NotFoundException
import invoice.management.system.api.PurchaseInvoicesApiDelegate
import invoice.management.system.entities.PurchaseInvoiceDocument
import invoice.management.system.model.PurchaseInvoiceDto
import invoice.management.system.repositories.PurchaseInvoiceRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import invoice.management.system.services.invoiceGeneration.mapper.toEntity
import invoice.management.system.services.invoiceGeneration.mapper.toResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class PurchaseInvoiceService(
    private val purchaseInvoiceRepository: PurchaseInvoiceRepository
) : PurchaseInvoicesApiDelegate {

    override fun createPurchaseInvoice(
        invoiceData: PurchaseInvoiceDto,
        pdf: Resource?
    ): ResponseEntity<PurchaseInvoiceDto> {

        val pdfBytes = try {
            pdf?.inputStream?.readBytes()
        } catch (ex: Exception) {
            return ResponseEntity.internalServerError().build()
        }

        if (pdfBytes?.size!! < 4 ||
            !pdfBytes.copyOfRange(0, 4)
                .contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46))
        ) {
            return ResponseEntity.badRequest().build()
        }

        val purchaseInvoiceDocument = PurchaseInvoiceDocument(pdfBytes)
        val purchaseInvoice = invoiceData.toEntity(purchaseInvoiceDocument)
        val savedPurchaseInvoice = purchaseInvoiceRepository.save(purchaseInvoice)
        return ResponseEntity(savedPurchaseInvoice.toDto(), HttpStatus.CREATED)
    }

    override fun getAllPurchaseInvoices(): ResponseEntity<List<PurchaseInvoiceDto>> {
        val purchaseInvoices = purchaseInvoiceRepository.findAll()
        return ResponseEntity(purchaseInvoices.map { it.toDto() }, HttpStatus.OK)
    }

    override fun getPurchaseInvoicePdf(id: Int): ResponseEntity<Resource> {
        val purchaseInvoice = purchaseInvoiceRepository.findById(id)
            .orElseThrow{ NotFoundException("purchase invoice with id $id not found") }

        val purchaseInvoiceDocument = if(purchaseInvoice.document != null) purchaseInvoice.document else throw NotFoundException("no document available")

        return ResponseEntity(purchaseInvoiceDocument?.toResource(), HttpStatus.OK)
    }
}
