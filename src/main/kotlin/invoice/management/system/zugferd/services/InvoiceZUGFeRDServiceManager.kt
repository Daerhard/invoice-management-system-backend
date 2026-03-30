package invoice.management.system.zugferd.services

import invoice.management.system.api.ConflictException
import invoice.management.system.api.NotFoundException
import invoice.management.system.entities.CardmarketOrder
import invoice.management.system.entities.Invoice
import invoice.management.system.model.ZugferdInvoiceDto
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.services.invoiceGeneration.mapper.toZugferdDto
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class InvoiceZUGFeRDServiceManager(
    private val cardmarketOrderRepository: CardmarketOrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
    private val zugferdGenerationService: ZugferdGenerationService,
) {

    fun getOrder(externalOrderId: Long): CardmarketOrder {
        return cardmarketOrderRepository.findByExternalOrderId(externalOrderId)
            ?: throw NotFoundException("Order with id $externalOrderId not found.")
    }

    fun checkNoExistingInvoice(order: CardmarketOrder) {
        if (invoiceRepository.findByOrder(order) != null) {
            throw ConflictException("Invoice already exists for order ${order.externalOrderId}.")
        }
    }

    fun createAndSaveInvoice(order: CardmarketOrder): Invoice {
        logger.info { "Creating ZUGFeRD invoice for order ${order.externalOrderId}" }
        val pdfBytes = invoicePDFGenerationService.generateInvoicePdf(order)
        val zugferdPdfBytes = zugferdGenerationService.generateZugferdInvoice(order, pdfBytes)
        val invoice = Invoice(
            order = order,
            createdAt = Instant.now(),
            invoicePdf = zugferdPdfBytes,
        )
        val savedInvoice = invoiceRepository.save(invoice)
        logger.info { "ZUGFeRD invoice saved with id ${savedInvoice.id} for order ${order.externalOrderId}" }
        return savedInvoice
    }

    fun buildCreatedResponse(invoice: Invoice): ResponseEntity<ZugferdInvoiceDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice.toZugferdDto())
    }

    fun getZugferdInvoice(id: Long): Invoice {
        return invoiceRepository.findById(id)
            .orElseThrow { NotFoundException("ZUGFeRD invoice with id $id not found.") }
    }

    fun buildOkResponse(invoice: Invoice): ResponseEntity<ZugferdInvoiceDto> {
        return ResponseEntity.ok(invoice.toZugferdDto())
    }
}
