package invoice.management.system.services.email

import invoice.management.system.entities.Invoice
import invoice.management.system.model.InvoiceEmailRequestDto
import invoice.management.system.repositories.InvoiceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class InvoiceEmailWorkflowService(
    private val invoiceRepository: InvoiceRepository,
    private val invoiceEmailPreparationService: InvoiceEmailPreparationService,
    private val emailService: EmailService,
) {

    fun sendInvoiceEmail(orderId: Long, request: InvoiceEmailRequestDto?): InvoiceEmailWorkflowResult {
        val invoice = invoiceRepository.findByOrderExternalOrderId(orderId)
            ?: return InvoiceEmailWorkflowResult.NotFound("Order with id $orderId not found.")

        val preparedEmail = invoiceEmailPreparationService.prepare(invoice, request)
            ?: return InvoiceEmailWorkflowResult.NotFound(
                "Customer has no email address and no recipient was provided."
            )

        return try {
            emailService.sendEmail(preparedEmail.emailRequest)
            updateSentAt(invoice)
            InvoiceEmailWorkflowResult.Success(preparedEmail.recipient)
        } catch (ex: EmailSendException) {
            logger.error(ex) { "Failed to send invoice email for order $orderId to ${preparedEmail.recipient}" }
            InvoiceEmailWorkflowResult.Failed("Failed to send invoice email: ${ex.message}")
        } catch (ex: Exception) {
            logger.error(ex) { "Invoice email was sent, but sentAt update failed for order $orderId" }
            InvoiceEmailWorkflowResult.Failed("Failed to update invoice status after sending email.")
        }
    }

    private fun updateSentAt(invoice: Invoice) {
        invoiceRepository.save(invoice.copy(sentAt = invoice.sentAt ?: Instant.now()))
    }
}

sealed class InvoiceEmailWorkflowResult {
    data class Success(val recipient: String) : InvoiceEmailWorkflowResult()
    data class NotFound(val message: String) : InvoiceEmailWorkflowResult()
    data class Failed(val message: String) : InvoiceEmailWorkflowResult()
}


