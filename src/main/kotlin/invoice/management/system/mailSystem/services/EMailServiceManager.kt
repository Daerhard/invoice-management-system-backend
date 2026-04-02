package invoice.management.system.mailSystem.services

import invoice.management.system.api.NotFoundException
import invoice.management.system.entities.Invoice
import invoice.management.system.mailSystem.entities.EmailAttachment
import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.model.EmailSendResponseDto
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.repositories.UserRepository
import jakarta.mail.Folder
import jakarta.mail.MessagingException
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.Properties

private val logger = KotlinLogging.logger {}

@Service
class EMailServiceManager(
    private val invoiceRepository: InvoiceRepository,
    private val userRepository: UserRepository,
    private val mailSender: JavaMailSender,
    @Value("\${imap.host:imap.gmx.net}") private val imapHost: String,
    @Value("\${imap.port:993}") private val imapPort: Int,
    @Value("\${imap.sent-folder:Gesendet}") private val imapSentFolder: String,
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
        val receiver = invoice.order.customer.email.takeIf { it != null }
            ?: throw IllegalArgumentException("Customer has no email address.")
        val user = userRepository.findAll().first().takeIf { it != null }
                ?: throw IllegalStateException("No sender user found in the database.")
        val sender = user.email

        logger.info { "Sending email to $receiver for Bestellnummer '${invoice.order.externalOrderId}" }

        val defaultEmail = getDefaultEmail(invoice)

        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(sender)
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
            copyToSentFolder(message)

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

    private fun copyToSentFolder(message: MimeMessage) {
        val senderImpl = mailSender as? JavaMailSenderImpl
        val username = senderImpl?.username
        val password = senderImpl?.password
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            logger.warn { "IMAP credentials not available – skipping copy to '$imapSentFolder' folder." }
            return
        }

        val props = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", imapHost)
            put("mail.imaps.port", imapPort.toString())
            put("mail.imaps.ssl.enable", "true")
        }

        val session = Session.getInstance(props)
        val store = session.getStore("imaps")
        try {
            store.connect(imapHost, imapPort, username, password)
            val sentFolder = store.getFolder(imapSentFolder)
            if (!sentFolder.exists()) {
                logger.warn { "IMAP folder '$imapSentFolder' does not exist – skipping copy to sent folder." }
            } else {
                sentFolder.open(Folder.READ_WRITE)
                sentFolder.appendMessages(arrayOf(message))
                sentFolder.close(false)
                logger.info { "Email successfully copied to IMAP folder '$imapSentFolder'." }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to copy sent email to IMAP folder '$imapSentFolder'." }
        } finally {
            if (store.isConnected) store.close()
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
