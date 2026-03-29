package invoice.management.system.services.email

import invoice.management.system.api.EmailApiDelegate
import invoice.management.system.model.EmailSendRequestDto
import invoice.management.system.model.EmailSendResponseDto
import invoice.management.system.model.InvoiceEmailRequestDto
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

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
    private val invoiceEmailWorkflowService: InvoiceEmailWorkflowService,
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
        return when (val result = invoiceEmailWorkflowService.sendInvoiceEmail(orderId, invoiceEmailRequestDto)) {
            is InvoiceEmailWorkflowResult.Success ->
                ResponseEntity.ok(
                    EmailSendResponseDto(
                        message = "Invoice email sent successfully to ${result.recipient}."
                    )
                )

            is InvoiceEmailWorkflowResult.NotFound ->
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(EmailSendResponseDto(message = result.message))

            is InvoiceEmailWorkflowResult.Failed ->
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailSendResponseDto(message = result.message))
        }
    }
}
