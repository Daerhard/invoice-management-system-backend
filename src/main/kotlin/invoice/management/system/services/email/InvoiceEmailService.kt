package invoice.management.system.services.email

import invoice.management.system.api.InvoiceEmailApiDelegate
import invoice.management.system.model.ResponseMessageDto
import invoice.management.system.model.TestSendInvoiceEmailRequestDto
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import mu.KotlinLogging
import org.springframework.mail.MailException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class InvoiceEmailService(
    private val emailService: EmailService,
    private val cardmarketOrderRepository: CardmarketOrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
) : InvoiceEmailApiDelegate {

    override fun sendInvoiceEmail(externalOrderId: Long): ResponseEntity<ResponseMessageDto> {
        val order = cardmarketOrderRepository.findByExternalOrderId(externalOrderId)
            ?: return ResponseEntity.notFound().build()

        val customerEmail = order.customer.email
        if (customerEmail == null) {
            return ResponseEntity.badRequest()
                .body(ResponseMessageDto(message = "Customer has no email address."))
        }

        val pdfBytes = resolveInvoicePdf(externalOrderId, order)
            ?: return ResponseEntity.notFound().build()

        return trySendEmail(customerEmail, externalOrderId, pdfBytes)
    }

    override fun testSendInvoiceEmail(
        testSendInvoiceEmailRequestDto: TestSendInvoiceEmailRequestDto
    ): ResponseEntity<ResponseMessageDto> {
        val bestellnummer = testSendInvoiceEmailRequestDto.bestellnummer
        val testEmail = testSendInvoiceEmailRequestDto.testEmail

        val order = cardmarketOrderRepository.findByExternalOrderId(bestellnummer)
            ?: return ResponseEntity.notFound().build()

        val pdfBytes = resolveInvoicePdf(bestellnummer, order)
            ?: return ResponseEntity.notFound().build()

        return trySendEmail(testEmail, bestellnummer, pdfBytes)
    }

    private fun resolveInvoicePdf(
        externalOrderId: Long,
        order: invoice.management.system.entities.CardmarketOrder
    ): ByteArray? {
        val invoice = invoiceRepository.findByOrder(order)
        return when {
            invoice?.invoicePdf != null -> invoice.invoicePdf
            invoice != null -> invoicePDFGenerationService.generateInvoicePdf(order)
            else -> {
                logger.warn { "No invoice found for order $externalOrderId." }
                null
            }
        }
    }

    private fun trySendEmail(
        email: String,
        bestellnummer: Long,
        pdfBytes: ByteArray
    ): ResponseEntity<ResponseMessageDto> {
        return try {
            emailService.sendInvoiceEmail(email, bestellnummer, pdfBytes)
            ResponseEntity.ok(ResponseMessageDto(message = "Invoice email sent to $email."))
        } catch (e: MailException) {
            logger.error(e) { "Failed to send invoice email for order $bestellnummer." }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessageDto(message = "Failed to send email: ${e.message}"))
        }
    }
}
