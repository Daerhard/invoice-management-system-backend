package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.OrderItem
import invoice.management.system.model.OrderItemDto

fun OrderItem.toDto() : OrderItemDto {
    return OrderItemDto(
        orderId = cardmarketOrder.externalOrderId,
        count = count,
        condition = condition,
        price = price,
        card = card.toDto()
    )
}