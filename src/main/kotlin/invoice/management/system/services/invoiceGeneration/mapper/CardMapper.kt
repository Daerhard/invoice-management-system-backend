package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.Card
import invoice.management.system.model.CardDto
import invoice.management.system.model.CardIdDto

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