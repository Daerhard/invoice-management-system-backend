package invoice.management.system.serviceTests

import invoice.management.system.model.EmailSendRequestDto
import invoice.management.system.model.InvoiceEmailRequestDto
import invoice.management.system.mailSystem.services.EmailApiDelegateService
import invoice.management.system.mailSystem.services.EmailRequest
import invoice.management.system.mailSystem.entities.EmailSendException
import invoice.management.system.mailSystem.services.EmailService
import invoice.management.system.mailSystem.services.InvoiceEmailWorkflowResult
import invoice.management.system.mailSystem.services.InvoiceEmailWorkflowService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
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
    private val invoiceEmailWorkflowService: InvoiceEmailWorkflowService =
        mock(InvoiceEmailWorkflowService::class.java)

    private val service = EmailApiDelegateService(
        emailService,
        invoiceEmailWorkflowService,
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
    fun whenSendInvoiceEmail_withWorkflowSuccess_thenReturn200() {
        `when`(invoiceEmailWorkflowService.sendInvoiceEmail(42L, null))
            .thenReturn(InvoiceEmailWorkflowResult.Success("customer@example.com"))

        val response = service.sendInvoiceEmail(42L, null)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Invoice email sent successfully to customer@example.com.", response.body?.message)
    }

    @Test
    fun whenSendInvoiceEmail_withWorkflowNotFound_thenReturn404() {
        `when`(invoiceEmailWorkflowService.sendInvoiceEmail(99L, null))
            .thenReturn(InvoiceEmailWorkflowResult.NotFound("Order with id 99 not found."))

        val response = service.sendInvoiceEmail(99L, null)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Order with id 99 not found.", response.body?.message)
    }

    @Test
    fun whenSendInvoiceEmail_withWorkflowFailure_thenReturn500() {
        val requestBody = InvoiceEmailRequestDto(to = "override@example.com", subject = "Custom")
        `when`(invoiceEmailWorkflowService.sendInvoiceEmail(42L, requestBody))
            .thenReturn(InvoiceEmailWorkflowResult.Failed("Failed to send invoice email: SMTP error"))

        val response = service.sendInvoiceEmail(42L, requestBody)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Failed to send invoice email: SMTP error", response.body?.message)
    }

    @Test
    fun whenSendEmail_thenInvoiceWorkflowIsNotCalled() {
        val request = EmailSendRequestDto(
            to = "customer@example.com",
            subject = "Test Subject",
            body = "Hello",
        )

        service.sendEmail(request)

        Mockito.verifyNoInteractions(invoiceEmailWorkflowService)
    }
}
