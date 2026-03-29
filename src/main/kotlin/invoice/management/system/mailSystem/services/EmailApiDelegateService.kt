package invoice.management.system.mailSystem.services

import invoice.management.system.api.EmailApiDelegate
import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.model.EmailSendRequestDto
import invoice.management.system.model.EmailSendResponseDto
import invoice.management.system.model.InvoiceEmailRequestDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class EmailApiDelegateService(
    private val emailService: EmailService,
    private val invoiceEmailWorkflowService: InvoiceEmailWorkflowService,
) : EmailApiDelegate {

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
            ResponseEntity.ok(EmailSendResponseDto("Email sent successfully."))
        } catch (ex: EmailSendException) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmailSendResponseDto(ex.message ?: "Failed to send email."))
        }
    }

    override fun sendInvoiceEmail(
        orderId: Long,
        invoiceEmailRequestDto: InvoiceEmailRequestDto?,
    ): ResponseEntity<EmailSendResponseDto> {
        return when (val result = invoiceEmailWorkflowService.sendInvoiceEmail(orderId, invoiceEmailRequestDto)) {
            is InvoiceEmailWorkflowResult.Success ->
                ResponseEntity.ok(EmailSendResponseDto("Invoice email sent successfully to ${result.recipient}."))
            is InvoiceEmailWorkflowResult.NotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(EmailSendResponseDto(result.message))
            is InvoiceEmailWorkflowResult.Failed ->
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailSendResponseDto(result.message))
        }
    }
}
