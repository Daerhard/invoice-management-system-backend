package invoice.management.system.services.invoiceGeneration

import invoice.management.system.api.InvoiceGenerationPDFApiDelegate
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.CustomerRepository
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class InvoicePDFService(
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
    private val cardmarketOrderRepository: CardmarketOrderRepository
) : InvoiceGenerationPDFApiDelegate {

    override fun getInvoiceForOrderAsPDF(cardmarketExternalOrderId: Long): ResponseEntity<Resource> {
        val cardmarketOrder = cardmarketOrderRepository.findByExternalOrderId(cardmarketExternalOrderId)
            ?: return ResponseEntity.notFound().build()

        val invoicePDF = invoicePDFGenerationService.generateInvoicePdf(cardmarketOrder)

        val resource = ByteArrayResource(invoicePDF)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(invoicePDF.size.toLong())
            .body(resource)
    }
}
