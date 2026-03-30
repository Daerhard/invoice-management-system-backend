package invoice.management.system.mailSystem.services

import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.model.InvoiceEmailRequestDto
import invoice.management.system.repositories.InvoiceRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class InvoiceEmailWorkflowService(
    private val invoiceRepository: InvoiceRepository,
    private val invoiceEmailPreparationService: InvoiceEmailPreparationService,
    private val emailService: EmailService,
) {

    fun sendInvoiceEmail(orderId: Long, requestDto: InvoiceEmailRequestDto?): InvoiceEmailWorkflowResult {
        val invoice = invoiceRepository.findByOrderExternalOrderId(orderId)
            ?: return InvoiceEmailWorkflowResult.NotFound("Order with id $orderId not found.")

        val prepared = invoiceEmailPreparationService.prepare(invoice, requestDto)
            ?: return InvoiceEmailWorkflowResult.NotFound("Customer has no email address and no recipient was provided.")

        return try {
            emailService.sendEmail(prepared.emailRequest)
            logger.info { "Invoice email sent to ${prepared.recipient} for order $orderId" }

            if (invoice.sentAt == null) {
                invoice.sentAt = java.time.Instant.now()
            }
            invoiceRepository.save(invoice)

            InvoiceEmailWorkflowResult.Success(prepared.recipient)
        } catch (ex: EmailSendException) {
            logger.error(ex) { "Failed to send invoice email to ${prepared.recipient} for order $orderId" }
            InvoiceEmailWorkflowResult.Failed("Failed to send invoice email: ${ex.message}")
        }
    }
}
