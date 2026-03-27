package invoice.management.system.services.invoiceGeneration

import invoice.management.system.api.OrdersApiDelegate
import invoice.management.system.entities.Invoice
import invoice.management.system.model.*
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.services.email.EmailService
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class CardmarketOrderService(
    private val cardmarketOrderRepository: CardmarketOrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
    private val emailService: EmailService,
) : OrdersApiDelegate {

    override fun getOrders(): ResponseEntity<List<CardmarketOrderDto>> {
        val cardmarketOrders = cardmarketOrderRepository.findAll()

        val cardmarketOrderDtos = cardmarketOrders.map { cardmarketOrder ->
            cardmarketOrder.toDto()
        }
        return ResponseEntity(cardmarketOrderDtos, HttpStatus.OK)
    }

    override fun getOrdersByUserName(userName: String): ResponseEntity<List<CardmarketOrderDto>> {
        val cardmarketOrders = cardmarketOrderRepository.findAll()

        val cardmarketOrderDtos = cardmarketOrders.filter {
            it.customer.userName == userName
        }.map { cardmarketOrder ->
            cardmarketOrder.toDto()
        }
        return ResponseEntity(cardmarketOrderDtos, HttpStatus.OK)
    }

    override fun createInvoice(externalOrderId: Long): ResponseEntity<InvoiceDto> {
        val order = cardmarketOrderRepository.findByExternalOrderId(externalOrderId)
            ?: return ResponseEntity.notFound().build()

        if (invoiceRepository.findByOrder(order) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        val pdfBytes = invoicePDFGenerationService.generateInvoicePdf(order)
        val invoice = Invoice(
            order = order,
            createdAt = Instant.now(),
            invoicePdf = pdfBytes,
        )
        val savedInvoice = invoiceRepository.save(invoice)

        try {
            emailService.sendInvoiceEmail(order.customer.email, externalOrderId, pdfBytes)
        } catch (e: Exception) {
            logger.error(e) { "Invoice created but email sending failed for order $externalOrderId." }
        }

        return ResponseEntity(savedInvoice.toDto(), HttpStatus.CREATED)
    }
}
