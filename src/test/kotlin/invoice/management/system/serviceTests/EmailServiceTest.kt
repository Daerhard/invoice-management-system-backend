package invoice.management.system.serviceTests

import invoice.management.system.services.email.EmailService
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.mail.javamail.JavaMailSender

class EmailServiceTest {

    private val mailSender: JavaMailSender = mock(JavaMailSender::class.java)
    private val emailService = EmailService(mailSender)
    private val fakePdf = "PDF".toByteArray()

    @Test
    fun whenCustomerEmailIsNull_thenNoEmailIsSent() {
        emailService.sendInvoiceEmail(null, 12345L, fakePdf)

        verifyNoInteractions(mailSender)
    }

    @Test
    fun whenCustomerEmailIsProvided_thenEmailIsSent() {
        val mimeMessage = mock(MimeMessage::class.java)
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)

        emailService.sendInvoiceEmail("customer@example.com", 12345L, fakePdf)

        verify(mailSender).send(mimeMessage)
    }
}
