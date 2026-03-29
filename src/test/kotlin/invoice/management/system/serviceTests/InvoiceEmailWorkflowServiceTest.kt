package invoice.management.system.serviceTests

import invoice.management.system.entities.Invoice
import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.factories.EntityFactory.Companion.createInvoice
import invoice.management.system.model.InvoiceEmailRequestDto
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.mailSystem.services.EmailRequest
import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.mailSystem.services.EmailService
import invoice.management.system.mailSystem.services.InvoiceEmailPreparationService
import invoice.management.system.mailSystem.services.InvoiceEmailWorkflowResult
import invoice.management.system.mailSystem.services.InvoiceEmailWorkflowService
import invoice.management.system.mailSystem.services.PreparedInvoiceEmail
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@Suppress("UNCHECKED_CAST")
private fun <T> anyNonNull(): T = Mockito.any<T>() as T

class InvoiceEmailWorkflowServiceTest {

    private val invoiceRepository: InvoiceRepository = mock(InvoiceRepository::class.java)
    private val invoiceEmailPreparationService: InvoiceEmailPreparationService =
        mock(InvoiceEmailPreparationService::class.java)
    private val emailService: EmailService = mock(EmailService::class.java)

    private val service = InvoiceEmailWorkflowService(
        invoiceRepository,
        invoiceEmailPreparationService,
        emailService,
    )

    @Test
    fun whenInvoiceMissing_thenReturnNotFound() {
        `when`(invoiceRepository.findByOrderExternalOrderId(42L)).thenReturn(null)

        val result = service.sendInvoiceEmail(42L, null)

        assertEquals(InvoiceEmailWorkflowResult.NotFound("Order with id 42 not found."), result)
    }

    @Test
    fun whenRecipientCannotBeResolved_thenReturnNotFound() {
        val invoice = createInvoice(order = createCardmarketOrder(orderId = 42L))
        `when`(invoiceRepository.findByOrderExternalOrderId(42L)).thenReturn(invoice)
        `when`(invoiceEmailPreparationService.prepare(invoice, null)).thenReturn(null)

        val result = service.sendInvoiceEmail(42L, null)

        assertEquals(
            InvoiceEmailWorkflowResult.NotFound("Customer has no email address and no recipient was provided."),
            result
        )
    }

    @Test
    fun whenSendSucceeds_thenSentAtIsSetAndSaved() {
        val invoice = createInvoice(
            order = createCardmarketOrder(orderId = 42L, customer = createCustomer(email = "customer@example.com")),
            invoicePdf = "PDF".toByteArray(),
        )
        val prepared = PreparedInvoiceEmail(
            recipient = "customer@example.com",
            emailRequest = EmailRequest(
                to = "customer@example.com",
                subject = "subject",
                body = "body",
            ),
        )

        `when`(invoiceRepository.findByOrderExternalOrderId(42L)).thenReturn(invoice)
        `when`(invoiceEmailPreparationService.prepare(invoice, null)).thenReturn(prepared)

        val result = service.sendInvoiceEmail(42L, null)

        assertEquals(InvoiceEmailWorkflowResult.Success("customer@example.com"), result)

        val captor = ArgumentCaptor.forClass(Invoice::class.java)
        verify(invoiceRepository).save(captor.capture())
        assertNotNull(captor.value.sentAt)
    }

    @Test
    fun whenInvoiceAlreadySent_thenSentAtIsNotOverridden() {
        val originalSentAt = Instant.parse("2024-01-01T10:00:00Z")
        val invoice = createInvoice(
            order = createCardmarketOrder(orderId = 42L, customer = createCustomer(email = "customer@example.com")),
            invoicePdf = "PDF".toByteArray(),
            sentAt = originalSentAt,
        )
        val prepared = PreparedInvoiceEmail(
            recipient = "customer@example.com",
            emailRequest = EmailRequest(
                to = "customer@example.com",
                subject = "subject",
                body = "body",
            ),
        )

        `when`(invoiceRepository.findByOrderExternalOrderId(42L)).thenReturn(invoice)
        `when`(invoiceEmailPreparationService.prepare(invoice, null)).thenReturn(prepared)

        service.sendInvoiceEmail(42L, null)

        val captor = ArgumentCaptor.forClass(Invoice::class.java)
        verify(invoiceRepository).save(captor.capture())
        assertEquals(originalSentAt, captor.value.sentAt)
    }

    @Test
    fun whenEmailSendingFails_thenReturnFailed() {
        val invoice = createInvoice(
            order = createCardmarketOrder(orderId = 42L, customer = createCustomer(email = "customer@example.com")),
            invoicePdf = "PDF".toByteArray(),
        )
        val requestBody = InvoiceEmailRequestDto(subject = "Custom")
        val prepared = PreparedInvoiceEmail(
            recipient = "customer@example.com",
            emailRequest = EmailRequest(
                to = "customer@example.com",
                subject = "subject",
                body = "body",
            ),
        )

        `when`(invoiceRepository.findByOrderExternalOrderId(42L)).thenReturn(invoice)
        `when`(invoiceEmailPreparationService.prepare(invoice, requestBody)).thenReturn(prepared)
        Mockito.doThrow(EmailSendException("SMTP error")).`when`(emailService)
            .sendEmail(anyNonNull())

        val result = service.sendInvoiceEmail(42L, requestBody)

        assertEquals(InvoiceEmailWorkflowResult.Failed("Failed to send invoice email: SMTP error"), result)
    }
}


