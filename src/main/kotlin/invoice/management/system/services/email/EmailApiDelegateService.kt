package invoice.management.system.services.email

import invoice.management.system.api.EmailApiDelegate
import invoice.management.system.model.EmailSendRequestDto
import invoice.management.system.model.EmailSendResponseDto
import invoice.management.system.model.InvoiceEmailRequestDto
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * Implements the [EmailApiDelegate] to handle all email-related HTTP endpoints
 * generated from the `email-openapi.yml` specification.
 *
 * Two operations are supported:
 * - [sendEmail]: sends a generic plain-text or HTML email.
 * - [sendInvoiceEmail]: generates a PDF invoice for a Cardmarket order and
 *   sends it to the customer's stored email address (or an overridden recipient).
 */
@Service
class EmailApiDelegateService(
    private val emailService: EmailService,
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
    private val cardmarketOrderRepository: CardmarketOrderRepository,
    private val invoiceRepository: InvoiceRepository,
) : EmailApiDelegate {

    /**
     * Sends a generic email as described by [emailSendRequestDto].
     *
     * Returns 200 on success, 500 when the mail cannot be delivered.
     */
    override fun sendEmail(emailSendRequestDto: EmailSendRequestDto): ResponseEntity<EmailSendResponseDto> {
        return try {
            emailService.sendEmail(
                EmailRequest(
                    to = emailSendRequestDto.to,
                    subject = emailSendRequestDto.subject,
                    body = emailSendRequestDto.body,
                    isHtml = emailSendRequestDto.isHtml ?: false,
                )
            )
            ResponseEntity.ok(EmailSendResponseDto(message = "Email sent successfully."))
        } catch (ex: EmailSendException) {
            logger.error(ex) { "Failed to send email to ${emailSendRequestDto.to}" }
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmailSendResponseDto(message = "Failed to send email: ${ex.message}"))
        }
    }

    /**
     * Generates the PDF invoice for [orderId] and emails it to the customer.
     *
     * The recipient address is taken from [invoiceEmailRequestDto]`.to` when present,
     * otherwise from the customer entity. Returns 404 when the order does not exist
     * or no email address can be resolved. Returns 500 when PDF generation or mail
     * delivery fails.
     */
    override fun sendInvoiceEmail(
        orderId: Long,
        invoiceEmailRequestDto: InvoiceEmailRequestDto?,
    ): ResponseEntity<EmailSendResponseDto> {
        val order = cardmarketOrderRepository.findByExternalOrderId(orderId)
            ?: return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(EmailSendResponseDto(message = "Order with id $orderId not found."))

        val recipient = invoiceEmailRequestDto?.to ?: order.customer.email
            ?: return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(EmailSendResponseDto(message = "Customer has no email address and no recipient was provided."))

        return try {
            val pdfBytes = invoicePDFGenerationService.generateInvoicePdf(order)

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val fileName =
                "${order.dateOfPayment.format(formatter)} Rechnung ${order.externalOrderId} - ${order.customer.fullName}.pdf"

            val subject = invoiceEmailRequestDto?.subject
                ?: "Ihre Rechnung – Bestellnummer ${order.externalOrderId}"

            emailService.sendEmail(
                EmailRequest(
                    to = recipient,
                    subject = subject,
                    body = "Sehr geehrter Kunde,\n\nim Anhang finden Sie Ihre Rechnung für Bestellnummer ${order.externalOrderId}.\n\nFreundliche Grüße",
                    attachments = listOf(EmailAttachment(fileName = fileName, content = pdfBytes)),
                )
            )
            invoiceRepository.findByOrder(order)?.let { invoice ->
                try {
                    invoiceRepository.save(invoice.copy(sentAt = Instant.now()))
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to update sentAt for invoice of order $orderId" }
                }
            }
            ResponseEntity.ok(EmailSendResponseDto(message = "Invoice email sent successfully to $recipient."))
        } catch (ex: EmailSendException) {
            logger.error(ex) { "Failed to send invoice email for order $orderId to $recipient" }
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmailSendResponseDto(message = "Failed to send invoice email: ${ex.message}"))
        }
    }
}
