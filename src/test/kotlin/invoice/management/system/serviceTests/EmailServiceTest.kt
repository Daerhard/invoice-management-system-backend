package invoice.management.system.serviceTests

import invoice.management.system.mailSystem.entities.EmailAttachment
import invoice.management.system.mailSystem.services.EmailRequest
import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.mailSystem.services.EmailService
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mail.MailAuthenticationException
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender

class EmailServiceTest {

    private val mailSender: JavaMailSender = mock(JavaMailSender::class.java)
    private val fromAddress = "sender@gmx.de"
    private val emailService = EmailService(mailSender, fromAddress)

    private fun createMimeMessage(): MimeMessage = MimeMessage(null as Session?)

    @Test
    fun whenSendEmail_withPlainTextBody_thenMailSenderIsCalled() {
        val mimeMessage = createMimeMessage()
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        val request = EmailRequest(
            to = "customer@example.com",
            subject = "Test Subject",
            body = "Hello, this is a test email.",
        )

        emailService.sendEmail(request)

        verify(mailSender).send(mimeMessage)
    }

    @Test
    fun whenSendEmail_withPdfAttachment_thenAttachmentIsIncluded() {
        val mimeMessage = createMimeMessage()
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        val pdfBytes = "PDF_CONTENT".toByteArray()
        val request = EmailRequest(
            to = "customer@example.com",
            subject = "Invoice",
            body = "Please find your invoice attached.",
            attachments = listOf(
                EmailAttachment(fileName = "invoice.pdf", content = pdfBytes),
            ),
        )

        emailService.sendEmail(request)

        verify(mailSender).send(mimeMessage)

        val multipart = mimeMessage.content as MimeMultipart
        val fileNames = (0 until multipart.count)
            .mapNotNull { multipart.getBodyPart(it).fileName }
        assertEquals(listOf("invoice.pdf"), fileNames)
    }

    @Test
    fun whenSendEmail_withHtmlBody_thenMailSenderIsCalled() {
        val mimeMessage = createMimeMessage()
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        val request = EmailRequest(
            to = "customer@example.com",
            subject = "HTML Email",
            body = "<h1>Hello</h1>",
            isHtml = true,
        )

        emailService.sendEmail(request)

        verify(mailSender).send(mimeMessage)
    }

    @Test
    fun whenSendEmail_andAuthenticationFails_thenThrowEmailSendException() {
        val mimeMessage = createMimeMessage()
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        doThrow(MailAuthenticationException("Bad credentials")).`when`(mailSender).send(mimeMessage)

        val request = EmailRequest(
            to = "customer@example.com",
            subject = "Test",
            body = "Body",
        )

        assertThrows(EmailSendException::class.java) {
            emailService.sendEmail(request)
        }
    }

    @Test
    fun whenSendEmail_andSmtpUnavailable_thenThrowEmailSendException() {
        val mimeMessage = createMimeMessage()
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        doThrow(MailSendException("Connection refused")).`when`(mailSender).send(mimeMessage)

        val request = EmailRequest(
            to = "customer@example.com",
            subject = "Test",
            body = "Body",
        )

        assertThrows(EmailSendException::class.java) {
            emailService.sendEmail(request)
        }
    }

    @Test
    fun whenSendEmail_withMultipleAttachments_thenAllAttachmentsAreIncluded() {
        val mimeMessage = createMimeMessage()
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        val request = EmailRequest(
            to = "customer@example.com",
            subject = "Multiple Attachments",
            body = "See attachments.",
            attachments = listOf(
                EmailAttachment(fileName = "invoice.pdf", content = "PDF1".toByteArray()),
                EmailAttachment(fileName = "summary.pdf", content = "PDF2".toByteArray()),
            ),
        )

        emailService.sendEmail(request)

        verify(mailSender).send(mimeMessage)

        val multipart = mimeMessage.content as MimeMultipart
        val fileNames = (0 until multipart.count)
            .mapNotNull { multipart.getBodyPart(it).fileName }
        assertEquals(listOf("invoice.pdf", "summary.pdf"), fileNames)
    }
}
