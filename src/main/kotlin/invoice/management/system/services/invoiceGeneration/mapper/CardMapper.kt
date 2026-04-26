package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.Card
import invoice.management.system.model.CardDto
import invoice.management.system.model.CardIdDto
import invoice.management.system.model.CardmarketPurchaseDto
import invoice.management.system.model.PurchaseItemCardDto
import invoice.management.system.model.PurchaseItemCardIdDto

fun Card.toDto() : CardDto {
    return CardDto(
        CardIdDto(
        konamiSet = cardId.konamiSet,
        number = cardId.konamiNumber,
        ),
        completeDescription = completeDescription,
        productName = productName,
        name = name,
        language = language,
        rarity = rarity,
        productId = productId
    )
}

fun Card.toPurchaseItemCardDto() :  PurchaseItemCardDto {
    return PurchaseItemCardDto(
        PurchaseItemCardIdDto(
            konamiSet = cardId.konamiSet,
            number = cardId.konamiNumber,
        ),
        completeDescription = completeDescription,
        productName = productName,
        name = name,
        language = language,
        rarity = rarity,
        productId = productId
    )
}