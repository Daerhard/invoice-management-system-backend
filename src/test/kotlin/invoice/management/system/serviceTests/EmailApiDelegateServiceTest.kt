package invoice.management.system.serviceTests

import invoice.management.system.entities.Invoice
import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.factories.EntityFactory.Companion.createInvoice
import invoice.management.system.model.EmailSendRequestDto
import invoice.management.system.model.EmailSendResponseDto
import invoice.management.system.model.InvoiceEmailRequestDto
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.services.email.EmailApiDelegateService
import invoice.management.system.services.email.EmailRequest
import invoice.management.system.services.email.EmailSendException
import invoice.management.system.services.email.EmailService
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus

/**
 * Returns a non-null typed null value so that Kotlin's call-site null-check is
 * not generated, yet Mockito still receives its argument matcher at runtime.
 * This is the standard workaround for using Mockito's `any()` with non-null
 * Kotlin parameters without the mockito-kotlin library.
 * NOTE: T must have NO upper bound so the `as T` cast is erased/unchecked
 * and does not produce a runtime null-pointer check.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> anyNonNull(): T = Mockito.any<T>() as T

class EmailApiDelegateServiceTest {

    private val emailService: EmailService = mock(EmailService::class.java)
    private val invoicePDFGenerationService: InvoicePDFGenerationService =
        mock(InvoicePDFGenerationService::class.java)
    private val cardmarketOrderRepository: CardmarketOrderRepository =
        mock(CardmarketOrderRepository::class.java)
    private val invoiceRepository: InvoiceRepository = mock(InvoiceRepository::class.java)

    private val service = EmailApiDelegateService(
        emailService,
        invoicePDFGenerationService,
        cardmarketOrderRepository,
        invoiceRepository,
    )

    // ========================== sendEmail ==========================

    @Test
    fun whenSendEmail_withValidRequest_thenReturn200() {
        val request = EmailSendRequestDto(
            to = "customer@example.com",
            subject = "Test Subject",
            body = "Hello",
        )

        val response = service.sendEmail(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Email sent successfully.", response.body?.message)
        verify(emailService).sendEmail(anyNonNull())
    }

    @Test
    fun whenSendEmail_andEmailServiceThrows_thenReturn500() {
        val request = EmailSendRequestDto(
            to = "customer@example.com",
            subject = "Test Subject",
            body = "Hello",
        )
        Mockito.doThrow(EmailSendException("Connection refused")).`when`(emailService)
            .sendEmail(anyNonNull<EmailRequest>())

        val response = service.sendEmail(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    // ========================== sendInvoiceEmail ==========================

    @Test
    fun whenSendInvoiceEmail_withCustomerEmail_thenReturn200() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(orderId = 42L, customer = customer)
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoicePDFGenerationService.generateInvoicePdf(order)).thenReturn("PDF".toByteArray())

        val response = service.sendInvoiceEmail(42L, null)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(emailService).sendEmail(anyNonNull())
    }

    @Test
    fun whenSendInvoiceEmail_withExistingInvoice_thenSentAtIsUpdated() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(orderId = 42L, customer = customer)
        val invoice = createInvoice(order = order)
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoicePDFGenerationService.generateInvoicePdf(order)).thenReturn("PDF".toByteArray())
        `when`(invoiceRepository.findByOrder(order)).thenReturn(invoice)

        service.sendInvoiceEmail(42L, null)

        val captor = ArgumentCaptor.forClass(Invoice::class.java)
        verify(invoiceRepository).save(captor.capture())
        assertNotNull(captor.value.sentAt)
    }

    @Test
    fun whenSendInvoiceEmail_withAlreadySentInvoice_thenSentAtIsNotOverridden() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(orderId = 42L, customer = customer)
        val originalSentAt = Instant.parse("2024-01-01T10:00:00Z")
        val invoice = createInvoice(order = order, sentAt = originalSentAt)
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoicePDFGenerationService.generateInvoicePdf(order)).thenReturn("PDF".toByteArray())
        `when`(invoiceRepository.findByOrder(order)).thenReturn(invoice)

        service.sendInvoiceEmail(42L, null)

        val captor = ArgumentCaptor.forClass(Invoice::class.java)
        verify(invoiceRepository).save(captor.capture())
        assertEquals(originalSentAt, captor.value.sentAt)
    }

    @Test
    fun whenSendInvoiceEmail_withOverriddenRecipient_thenReturn200() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(orderId = 42L, customer = customer)
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoicePDFGenerationService.generateInvoicePdf(order)).thenReturn("PDF".toByteArray())

        val requestBody = InvoiceEmailRequestDto(to = "override@example.com")
        val response = service.sendInvoiceEmail(42L, requestBody)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(emailService).sendEmail(anyNonNull())
    }

    @Test
    fun whenSendInvoiceEmail_withOrderNotFound_thenReturn404() {
        `when`(cardmarketOrderRepository.findByExternalOrderId(99L)).thenReturn(null)

        val response = service.sendInvoiceEmail(99L, null)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        verify(emailService, never()).sendEmail(anyNonNull())
    }

    @Test
    fun whenSendInvoiceEmail_withNoEmailAndNoOverride_thenReturn404() {
        val customer = createCustomer(email = null)
        val order = createCardmarketOrder(orderId = 1L, customer = customer)
        `when`(cardmarketOrderRepository.findByExternalOrderId(1L)).thenReturn(order)

        val response = service.sendInvoiceEmail(1L, null)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        verify(emailService, never()).sendEmail(anyNonNull())
    }

    @Test
    fun whenSendInvoiceEmail_andEmailServiceThrows_thenReturn500() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(orderId = 42L, customer = customer)
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoicePDFGenerationService.generateInvoicePdf(order)).thenReturn("PDF".toByteArray())
        Mockito.doThrow(EmailSendException("SMTP error")).`when`(emailService)
            .sendEmail(anyNonNull<EmailRequest>())

        val response = service.sendInvoiceEmail(42L, null)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun whenSendInvoiceEmail_withCustomSubject_thenReturn200() {
        val customer = createCustomer(email = "customer@example.com")
        val order = createCardmarketOrder(orderId = 42L, customer = customer)
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoicePDFGenerationService.generateInvoicePdf(order)).thenReturn("PDF".toByteArray())

        val requestBody = InvoiceEmailRequestDto(subject = "Custom Subject")
        val response = service.sendInvoiceEmail(42L, requestBody)

        assertEquals(HttpStatus.OK, response.statusCode)
        verify(emailService).sendEmail(anyNonNull())
    }
}
