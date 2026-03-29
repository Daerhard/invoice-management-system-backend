package invoice.management.system.services.email

import jakarta.mail.MessagingException
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.Properties

private val logger = KotlinLogging.logger {}

/**
 * Service for sending e-mails via the configured SMTP server (GMX by default).
 *
 * All SMTP credentials and connection settings are read from the application
 * configuration so that no secrets need to be hardcoded.
 *
 * Usage example:
 * ```kotlin
 * val request = EmailRequest(
 *     to      = "customer@example.com",
 *     subject = "Your invoice",
 *     body    = "Please find your invoice attached.",
 *     attachments = listOf(
 *         EmailAttachment(fileName = "invoice.pdf", content = pdfBytes)
 *     )
 * )
 * emailService.sendEmail(request)
 * ```
 */
@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${email.from}") private val fromAddress: String,
    @Value("\${email.imap.host:imap.gmx.net}") private val imapHost: String,
    @Value("\${email.imap.port:993}") private val imapPort: Int,
    @Value("\${email.imap.username:}") private val imapUsername: String,
    @Value("\${email.imap.password:}") private val imapPassword: String,
    @Value("\${email.imap.sent-folder:Gesendet}") private val sentFolderName: String,
) {

    /**
     * Sends an e-mail described by [request].
     *
     * The message is built as `multipart/mixed` when attachments are present so
     * that PDF files (or any other binaries) can be included alongside the body.
     *
     * @param request the fully configured [EmailRequest].
     * @throws EmailSendException when the mail cannot be delivered due to an
     *   authentication error, a network/SMTP failure, or any other problem.
     */
    fun sendEmail(request: EmailRequest) {
        logger.info { "Sending email to ${request.to} with subject '${request.subject}'" }

        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, request.attachments.isNotEmpty(), "UTF-8")

            helper.setFrom(fromAddress)
            helper.setTo(request.to)
            helper.setSubject(request.subject)
            helper.setText(request.body, request.isHtml)

            request.attachments.forEach { attachment ->
                helper.addAttachment(
                    attachment.fileName,
                    { attachment.content.inputStream() },
                    attachment.contentType,
                )
            }

            mailSender.send(message)
            logger.info { "Email successfully sent to ${request.to}" }
            saveCopyToSentFolder(message)

        } catch (ex: MailAuthenticationException) {
            logger.error(ex) { "SMTP authentication failed while sending email to ${request.to}" }
            throw EmailSendException("SMTP authentication failed: ${ex.message}", ex)
        } catch (ex: MailSendException) {
            logger.error(ex) { "Failed to send email to ${request.to}" }
            throw EmailSendException("Failed to send email: ${ex.message}", ex)
        } catch (ex: MessagingException) {
            logger.error(ex) { "Failed to build MIME message for ${request.to}" }
            throw EmailSendException("Failed to build email message: ${ex.message}", ex)
        }
    }

    private fun saveCopyToSentFolder(message: MimeMessage) {
        if (imapUsername.isBlank() || imapPassword.isBlank()) {
            logger.warn { "Skipping IMAP sent-copy: credentials are not configured." }
            return
        }

        val properties = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", imapHost)
            put("mail.imaps.port", imapPort.toString())
            put("mail.imaps.ssl.enable", "true")
        }

        var store: jakarta.mail.Store? = null
        var folder: jakarta.mail.Folder? = null

        try {
            val session = Session.getInstance(properties)
            store = session.getStore("imaps")
            store.connect(imapHost, imapPort, imapUsername, imapPassword)

            val folderCandidates = listOf(sentFolderName, "Gesendet", "Sent").distinct()
            folder = folderCandidates
                .asSequence()
                .map { store.getFolder(it) }
                .firstOrNull { it.exists() }
                ?: store.getFolder(sentFolderName)

            if (!folder.exists()) {
                folder.create(jakarta.mail.Folder.HOLDS_MESSAGES)
            }

            folder.open(jakarta.mail.Folder.READ_WRITE)
            folder.appendMessages(arrayOf(message))
            logger.info { "Saved sent email copy to IMAP folder '${folder.fullName}'." }
        } catch (ex: Exception) {
            // Best-effort only: sending already succeeded, so do not fail the request.
            logger.warn(ex) { "Email sent, but could not save a copy to IMAP sent folder." }
        } finally {
            try {
                folder?.close(false)
            } catch (_: Exception) {
            }
            try {
                store?.close()
            } catch (_: Exception) {
            }
        }
    }
}
