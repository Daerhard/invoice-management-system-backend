package invoice.management.system.mailSystem.services

import invoice.management.system.api.NotFoundException
import invoice.management.system.entities.Invoice
import invoice.management.system.mailSystem.entities.EmailAttachment
import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.model.EmailSendResponseDto
import invoice.management.system.repositories.InvoiceRepository
import jakarta.mail.MessagingException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class EMailServiceManager(
    private val invoiceRepository: InvoiceRepository,
    private val mailSender: JavaMailSender
) {

    fun getInvoice(orderId: Long): Invoice {
        return invoiceRepository.findByOrderExternalOrderId(orderId)
         ?: throw NotFoundException("Invoice for order with id $orderId not found.")
    }

    fun updateSentAt(invoice: Invoice): Invoice {
        invoice.sentAt = java.time.Instant.now()
        return invoiceRepository.save(invoice)
    }

    fun setResponseMessage(message: String): ResponseEntity<EmailSendResponseDto> {
        return ResponseEntity(EmailSendResponseDto(message),HttpStatus.OK)
    }

    fun sendEmail(invoice: Invoice) {
        val receiver = invoice.order.customer.email
            ?: throw IllegalArgumentException("Customer has no email address.")
        logger.info { "Sending email to $receiver for Bestellnummer '${invoice.order.externalOrderId}" }

        val defaultEmail = getDefaultEmail(invoice)

        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(receiver)
            helper.setSubject(defaultEmail.subject)
            helper.setText(defaultEmail.body, false)
            helper.addAttachment(
                defaultEmail.attachment.fileName,
                { defaultEmail.attachment.content.inputStream() },
                "application/pdf"
            )

            mailSender.send(message)
            logger.info { "Email successfully sent to $receiver" }

        } catch (ex: MailAuthenticationException) {
            logger.error(ex) { "SMTP authentication failed while sending email to $receiver" }
            throw EmailSendException("SMTP authentication failed: ${ex.message}", ex)
        } catch (ex: MailSendException) {
            logger.error(ex) { "Failed to send email to $receiver" }
            throw EmailSendException("Failed to send email: ${ex.message}", ex)
        } catch (ex: MessagingException) {
            logger.error(ex) { "Failed to build MIME message for $receiver" }
            throw EmailSendException("Failed to build email message: ${ex.message}", ex)
        }
    }

    private fun getDefaultEmail(invoice: Invoice): DefaultEmail {
        val pdfBytes = invoice.invoicePdf ?: throw IllegalArgumentException("Invoice pdf does not exist.")

        val subject = "Ihre Rechnung - Bestellnummer ${invoice.order.externalOrderId}"

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val order = invoice.order
        val fileName =
            "${order.dateOfPayment.format(formatter)} Rechnung ${order.externalOrderId} - ${order.customer.fullName}.pdf"

        return DefaultEmail(
            subject = subject,
            body = "Sehr geehrter Kunde,\n\nim Anhang finden Sie Ihre Rechnung, Bestellnummer ${order.externalOrderId}.\n\nFreundliche Gruesse",
            attachment = EmailAttachment(fileName = fileName, content = pdfBytes),
        )
    }
}

data class DefaultEmail(
    val subject: String,
    val body: String,
    val attachment: EmailAttachment,
)
