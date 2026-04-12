package invoice.management.system.services.purchaseInvoice

import invoice.management.system.api.NotFoundException
import invoice.management.system.api.PurchaseInvoicesApiDelegate
import invoice.management.system.entities.PurchaseInvoiceDocument
import invoice.management.system.model.PurchaseInvoiceDto
import invoice.management.system.model.PurchaseInvoiceItemDto
import invoice.management.system.repositories.PurchaseInvoiceItemRepository
import invoice.management.system.repositories.PurchaseInvoiceRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import invoice.management.system.services.invoiceGeneration.mapper.toEntity
import invoice.management.system.services.invoiceGeneration.mapper.toResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PurchaseInvoiceService(
    private val purchaseInvoiceRepository: PurchaseInvoiceRepository,
    private val purchaseInvoiceItemRepository: PurchaseInvoiceItemRepository
) : PurchaseInvoicesApiDelegate {

    override fun createPurchaseInvoice(
        purchaseInvoice: PurchaseInvoiceDto
    ): ResponseEntity<PurchaseInvoiceDto> {
        val entity = purchaseInvoice.toEntity()
        val saved = purchaseInvoiceRepository.save(entity)
        return ResponseEntity(saved.toDto(), HttpStatus.CREATED)
    }

    override fun addPurchaseInvoiceItem(
        id: Int,
        itemData: PurchaseInvoiceItemDto,
        pdf: Resource?
    ): ResponseEntity<PurchaseInvoiceDto> {
        val purchaseInvoice = purchaseInvoiceRepository.findById(id)
            .orElseThrow { NotFoundException("purchase invoice with id $id not found") }

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

        val item = itemData.toEntity()
        purchaseInvoice.addItem(item)

        if (pdfBytes != null) {
            val document = PurchaseInvoiceDocument(pdfBytes)
            item.attachDocument(document)
        }

        val saved = purchaseInvoiceRepository.save(purchaseInvoice)
        return ResponseEntity(saved.toDto(), HttpStatus.CREATED)
    }

    override fun getAllPurchaseInvoices(): ResponseEntity<List<PurchaseInvoiceDto>> {
        val purchaseInvoices = purchaseInvoiceRepository.findAll()
        return ResponseEntity(purchaseInvoices.map { it.toDto() }, HttpStatus.OK)
    }

    override fun getPurchaseInvoiceItemPdf(id: Int, itemId: Int): ResponseEntity<Resource> {
        purchaseInvoiceRepository.findById(id)
            .orElseThrow { NotFoundException("purchase invoice with id $id not found") }

        val item = purchaseInvoiceItemRepository.findById(itemId)
            .orElseThrow { NotFoundException("purchase invoice item with id $itemId not found") }

        val document = item.document ?: throw NotFoundException("no document available for item $itemId")

        return ResponseEntity(document.toResource(), HttpStatus.OK)
    }

    @Transactional
    override fun deletePurchaseInvoiceItem(id: Int, itemId: Int): ResponseEntity<Unit> {
        val invoice = purchaseInvoiceRepository.findById(id)
            .orElseThrow { NotFoundException("purchase invoice with id $id not found") }

        val item = purchaseInvoiceItemRepository.findById(itemId)
            .orElseThrow { NotFoundException("purchase invoice item with id $itemId not found") }

        if (item.invoice?.id != invoice.id) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        invoice.removeItem(item)
        purchaseInvoiceRepository.save(invoice)
        return ResponseEntity.noContent().build()
    }
}

