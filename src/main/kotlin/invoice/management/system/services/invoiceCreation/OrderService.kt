package invoice.management.system.services.invoiceCreation

import invoice.management.system.api.OrdersApiDelegate
import invoice.management.system.model.CardDto
import invoice.management.system.model.CardIdDto
import invoice.management.system.model.OrderDto
import invoice.management.system.model.OrderItemDto
import invoice.management.system.repositories.OrderRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository
) : OrdersApiDelegate {

    override fun getOrdersByUserName(userName: String): ResponseEntity<List<OrderDto>> {
        val purchases = orderRepository.findAll()

        val purchaseDtos = purchases.filter {
            it.customer.userName == userName
        }.map { purchase ->
            OrderDto(
                commission = purchase.commission,
                currency = purchase.currency,
                orderId = purchase.externalOrderId,
                articleCount = purchase.articleCount,
                merchandiseValue = purchase.merchandiseValue,
                shipmentCost = purchase.shipmentCost,
                totalValue = purchase.totalValue,
                purchaseItems = purchase.orderItems.map { item ->
                   OrderItemDto(
                        orderId = item.cardmarketOrder.externalOrderId,
                        count = item.count,
                        condition = item.condition,
                        price = item.price,
                        card =
                        CardDto(
                            CardIdDto(
                            konamiSet = item.card.cardId.konamiSet,
                            number = item.card.cardId.number,
                            ),
                            completeDescription = item.card.completeDescription,
                            productName = item.card.productName,
                            name = item.card.name,
                            language = item.card.language,
                            rarity = item.card.rarity,
                            productId = item.card.productId
                        )
                    )
                }
            )
        }
        return ResponseEntity(purchaseDtos, HttpStatus.OK)
    }
}