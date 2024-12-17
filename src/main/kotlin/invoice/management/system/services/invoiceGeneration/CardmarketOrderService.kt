package invoice.management.system.services.invoiceGeneration

import invoice.management.system.api.OrdersApiDelegate
import invoice.management.system.model.*
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class CardmarketOrderService(
    private val cardmarketOrderRepository: CardmarketOrderRepository
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
}