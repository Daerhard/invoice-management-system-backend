package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.CardmarketOrder
import invoice.management.system.model.CardmarketOrderDto

fun CardmarketOrder.toDto() : CardmarketOrderDto {
    return CardmarketOrderDto(
        customer = customer.toDto(),
        commission = commission,
        currency = currency,
        orderId = externalOrderId,
        articleCount = articleCount,
        merchandiseValue = merchandiseValue,
        shipmentCost = shipmentCost,
        totalValue = totalValue,
        orderItems = orderItems.map { orderItem ->
            orderItem.toDto()
        }
    )
}