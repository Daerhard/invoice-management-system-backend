package invoice.management.system.services.email

import jakarta.mail.internet.MimeMessage
import mu.KotlinLogging
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {

    fun sendInvoiceEmail(customerEmail: String?, bestellnummer: Long, pdfFile: ByteArray) {
        if (customerEmail == null) {
            logger.info { "No email address for order $bestellnummer – skipping email." }
            return
        }

        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(customerEmail)
        helper.setSubject("Bestellnummer $bestellnummer")
        helper.setText(
            """
            Guten Tag,

            die Rechnung zu Ihrer Bestellung $bestellnummer.

            Viele Grüße
            Daniel Erhard
            """.trimIndent()
        )
        helper.addAttachment("Rechnung_$bestellnummer.pdf", { pdfFile.inputStream() }, "application/pdf")

        try {
            mailSender.send(message)
            logger.info { "Invoice email sent to $customerEmail for order $bestellnummer." }
        } catch (e: MailException) {
            logger.error(e) { "Failed to send invoice email to $customerEmail for order $bestellnummer." }
            throw e
        }
    }
}
