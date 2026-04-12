package invoice.management.system.services.invoiceGeneration

import invoice.management.system.api.OrdersApiDelegate
import invoice.management.system.entities.Invoice
import invoice.management.system.model.*
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.repositories.OrderItemRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CardmarketOrderService(
    private val cardmarketOrderRepository: CardmarketOrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
    private val orderItemRepository: OrderItemRepository,
) : OrdersApiDelegate {

    @Transactional(readOnly = true)
    override fun getOrders(): ResponseEntity<List<CardmarketOrderDto>> {
        val cardmarketOrders = cardmarketOrderRepository.findAll()

        val cardmarketOrderDtos = cardmarketOrders.map { cardmarketOrder ->
            cardmarketOrder.toDto()
        }
        return ResponseEntity(cardmarketOrderDtos, HttpStatus.OK)
    }

    @Transactional(readOnly = true)
    override fun getOrdersByUserName(userName: String): ResponseEntity<List<CardmarketOrderDto>> {
        val cardmarketOrders = cardmarketOrderRepository.findByCustomerUserName(userName)

        val cardmarketOrderDtos = cardmarketOrders.map { cardmarketOrder ->
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
        return ResponseEntity(savedInvoice.toDto(), HttpStatus.CREATED)
    }

    @Transactional
    override fun deleteOrderItem(externalOrderId: Long, itemId: Int): ResponseEntity<Unit> {
        val order = cardmarketOrderRepository.findByExternalOrderId(externalOrderId)
            ?: return ResponseEntity.notFound().build()

        val item = orderItemRepository.findById(itemId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (item.cardmarketOrder.id != order.id) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }

        order.orderItems.remove(item)
        cardmarketOrderRepository.save(order)
        return ResponseEntity.noContent().build()
    }
}
