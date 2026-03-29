package invoice.management.system.mailSystem.services

import invoice.management.system.api.EmailApi
import invoice.management.system.api.EmailApiDelegate
import invoice.management.system.model.EmailSendResponseDto
import invoice.management.system.model.InvoiceEmailRequestDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class EMailService(
    private val eMailServiceManager: EMailServiceManager
): EmailApiDelegate {

    override fun sendInvoiceEmail(
        orderId: Long,
        invoiceEmailRequestDto: InvoiceEmailRequestDto?
    ): ResponseEntity<EmailSendResponseDto> {
        return orderId
        .let { id -> eMailServiceManager.getInvoice(id) }
        .also { invoice -> eMailServiceManager.sendEmail(invoice) }
        .let { invoice -> eMailServiceManager.updateSentAt(invoice) }
        .let { eMailServiceManager.setResponseMessage("E-Mail wurde erfolgreich versendet") }
    }
}