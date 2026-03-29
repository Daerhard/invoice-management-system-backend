package invoice.management.system.zugferd.services

import invoice.management.system.entities.Invoice
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class ZugferdManager(
    private val cardmarketOrderRepository: CardmarketOrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
    private val zugferdGenerationService: ZugferdGenerationService,
) {

    fun createAndSaveZugferdInvoice(externalOrderId: Long): ZugferdInvoiceResult {
        val order = cardmarketOrderRepository.findByExternalOrderId(externalOrderId)
            ?: return ZugferdInvoiceResult.OrderNotFound("Order with id $externalOrderId not found.")

        if (invoiceRepository.findByOrder(order) != null) {
            return ZugferdInvoiceResult.AlreadyExists("Invoice already exists for order $externalOrderId.")
        }

        logger.info { "Creating ZUGFeRD invoice for order $externalOrderId" }

        val pdfBytes = invoicePDFGenerationService.generateInvoicePdf(order)
        val zugferdPdfBytes = zugferdGenerationService.generateZugferdInvoice(order, pdfBytes)

        val invoice = Invoice(
            order = order,
            createdAt = Instant.now(),
            invoicePdf = zugferdPdfBytes,
        )
        val savedInvoice = invoiceRepository.save(invoice)

        logger.info { "ZUGFeRD invoice saved with id ${savedInvoice.id} for order $externalOrderId" }
        return ZugferdInvoiceResult.Success(savedInvoice)
    }

    fun findById(id: Long): Invoice? = invoiceRepository.findById(id).orElse(null)
}
