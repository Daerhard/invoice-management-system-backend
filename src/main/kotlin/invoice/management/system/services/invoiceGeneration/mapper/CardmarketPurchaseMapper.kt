package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.CardmarketPurchase
import invoice.management.system.entities.PurchaseItem
import invoice.management.system.model.CardmarketPurchaseDto
import invoice.management.system.model.PurchaseItemDto

fun CardmarketPurchase.toDto(): CardmarketPurchaseDto {
    return CardmarketPurchaseDto(
        id = id,
        sellerUserName = sellerUserName,
        externalOrderId = externalOrderId,
        dateOfPayment = dateOfPayment,
        articleCount = articleCount,
        merchandiseValue = merchandiseValue,
        shipmentCost = shipmentCost,
        trusteeFee = trusteeFee,
        totalValue = totalValue,
        currency = currency,
        purchaseItems = purchaseItems.map { it.toDto() }
    )
}

fun PurchaseItem.toDto(): PurchaseItemDto {
    return PurchaseItemDto(
        id = id,
        purchaseId = cardmarketPurchase.id,
        count = count,
        condition = condition,
        price = price,
        isFirstEdition = isFirstEdition,
        card = card.toDto()
    )
}
