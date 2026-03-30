package invoice.management.system.mailSystem.services

import invoice.management.system.mailSystem.entities.EmailSendException
import jakarta.mail.MessagingException
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${email.from}") private val fromAddress: String,
) {

    fun sendEmail(request: EmailRequest) {
        logger.info { "Sending email to ${request.to}" }

        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom(fromAddress)
            helper.setTo(request.to)
            helper.setSubject(request.subject)
            helper.setText(request.body, request.isHtml)

            for (attachment in request.attachments) {
                helper.addAttachment(
                    attachment.fileName,
                    { attachment.content.inputStream() },
                    "application/pdf",
                )
            }

            mailSender.send(message)
            logger.info { "Email successfully sent to ${request.to}" }

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
}
