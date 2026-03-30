package invoice.management.system.zugferd.services

import invoice.management.system.api.ZugferdApiDelegate
import invoice.management.system.model.ZugferdInvoiceDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class InvoiceZUGFeRDService(
    private val invoiceZUGFeRDServiceManager: InvoiceZUGFeRDServiceManager
) : ZugferdApiDelegate {

    override fun createZugferdInvoice(cardmarketExternalOrderId: Long): ResponseEntity<ZugferdInvoiceDto> {
        return cardmarketExternalOrderId
        .let { id -> invoiceZUGFeRDServiceManager.getOrder(id) }
        .also { order -> invoiceZUGFeRDServiceManager.checkNoExistingInvoice(order) }
        .let { order -> invoiceZUGFeRDServiceManager.createAndSaveInvoice(order) }
        .let { invoice -> invoiceZUGFeRDServiceManager.buildCreatedResponse(invoice) }
    }

    override fun getZugferdInvoiceById(id: Long): ResponseEntity<ZugferdInvoiceDto> {
        return id
        .let { invoiceId -> invoiceZUGFeRDServiceManager.getZugferdInvoice(invoiceId) }
        .let { invoice -> invoiceZUGFeRDServiceManager.buildOkResponse(invoice) }
    }
}
