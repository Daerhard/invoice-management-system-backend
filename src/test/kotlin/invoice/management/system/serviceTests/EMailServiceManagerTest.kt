package invoice.management.system.serviceTests

import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.factories.EntityFactory.Companion.createInvoice
import invoice.management.system.factories.EntityFactory.Companion.createUser
import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.mailSystem.services.EMailServiceManager
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.repositories.UserRepository
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSenderImpl

class EMailServiceManagerTest {

    private val invoiceRepository: InvoiceRepository = mock(InvoiceRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val mailSender: JavaMailSenderImpl = mock(JavaMailSenderImpl::class.java)

    private val manager = EMailServiceManager(
        invoiceRepository = invoiceRepository,
        userRepository = userRepository,
        mailSender = mailSender,
        imapHost = "imap.gmx.net",
        imapPort = 993,
        imapSentFolder = "Gesendet",
    )

    @Test
    fun `sendEmail throws EmailSendException when SMTP transport fails`() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(customer = customer)
        val invoice = createInvoice(order = order, invoicePdf = ByteArray(1) { 0 })
        val mimeMessage = mock(MimeMessage::class.java)

        `when`(userRepository.findAll()).thenReturn(listOf(createUser(email = "sender@gmx.de")))
        `when`(mailSender.createMimeMessage()).thenReturn(mimeMessage)
        `when`(mailSender.send(mimeMessage)).thenThrow(MailSendException("connection refused"))

        assertThrows(EmailSendException::class.java) {
            manager.sendEmail(invoice)
        }
    }

    @Test
    fun `sendEmail throws IllegalArgumentException when customer has no email`() {
        val customer = createCustomer(email = null)
        val order = createCardmarketOrder(customer = customer)
        val invoice = createInvoice(order = order, invoicePdf = ByteArray(1) { 0 })

        `when`(userRepository.findAll()).thenReturn(listOf(createUser()))

        assertThrows(IllegalArgumentException::class.java) {
            manager.sendEmail(invoice)
        }
    }

    @Test
    fun `sendEmail throws IllegalStateException when no user exists in database`() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(customer = customer)
        val invoice = createInvoice(order = order, invoicePdf = ByteArray(1) { 0 })

        `when`(userRepository.findAll()).thenReturn(emptyList())

        assertThrows(IllegalStateException::class.java) {
            manager.sendEmail(invoice)
        }
    }
}
