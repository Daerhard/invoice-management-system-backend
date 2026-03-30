package invoice.management.system.mailSystem.services

import invoice.management.system.api.EmailApiDelegate
import invoice.management.system.mailSystem.entities.EmailAttachment
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
            ResponseEntity(EmailSendResponseDto("Email sent successfully."), HttpStatus.OK)
        } catch (ex: EmailSendException) {
            ResponseEntity(EmailSendResponseDto(ex.message ?: "Failed to send email."), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    override fun sendInvoiceEmail(
        orderId: Long,
        invoiceEmailRequestDto: InvoiceEmailRequestDto?,
    ): ResponseEntity<EmailSendResponseDto> {
        return when (val result = invoiceEmailWorkflowService.sendInvoiceEmail(orderId, invoiceEmailRequestDto)) {
            is InvoiceEmailWorkflowResult.Success ->
                ResponseEntity(EmailSendResponseDto("Invoice email sent successfully to ${result.recipient}."), HttpStatus.OK)
            is InvoiceEmailWorkflowResult.NotFound ->
                ResponseEntity(EmailSendResponseDto(result.message), HttpStatus.NOT_FOUND)
            is InvoiceEmailWorkflowResult.Failed ->
                ResponseEntity(EmailSendResponseDto(result.message), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
