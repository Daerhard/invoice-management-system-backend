package invoice.management.system.factories

import invoice.management.system.entities.*
import java.time.LocalDate

class EntityFactory {

    companion object {

        fun createCustomer(
            userName: String = "fakeJohnny",
            fullName: String = "Johnny Test",
            street: String = "street",
            city: String = "city",
            country: String = "Germany",
            isProfessional: Boolean = true,
            vatNumber: String = "DE123456789",
        ) = Customer(
            userName = userName,
            fullName = fullName,
            street = street,
            city = city,
            country = country,
            isProfessional = isProfessional,
            vatNumber = vatNumber
        )

        fun createCardmarketOrder(
            id: Int? = null,
            customer: Customer = createCustomer(),
            orderId: Long = 1,
            dateOfPayment: LocalDate = LocalDate.now(),
            articleCount: Int = 1,
            merchandiseValue: Double = 1.0,
            shipmentCost: Double = 1.0,
            totalValue: Double = 1.0,
            commission: Double = 1.0,
            currency: String = "EUR",
            orderItems: MutableList<OrderItem> = mutableListOf()
        ) = CardmarketOrder(
            id = id,
            customer = customer,
            externalOrderId = orderId,
            dateOfPayment = dateOfPayment,
            articleCount = articleCount,
            merchandiseValue = merchandiseValue,
            shipmentCost = shipmentCost,
            totalValue = totalValue,
            commission = commission,
            currency = currency,
            orderItems = orderItems
        )

        fun createOrderItem(
            id: Int? = null,
            cardmarketOrder: CardmarketOrder = createCardmarketOrder(),
            count: Int = 1,
            condition: String = "new",
            price: Double = 1.0,
            isFirstEdition: Boolean = true,
            card: Card = createCard()
        ) = OrderItem(
            id = id,
            cardmarketOrder = cardmarketOrder,
            count = count,
            condition = condition,
            price = price,
            isFirstEdition = isFirstEdition,
            card = card
        )

        fun createCard(
            cardId: CardId = CardId("25th Anniversary Tin: Dueling Mirrors", "147"),
            completeDescription: String = "1x Trident Dragion (25th Anniversary Tin: Dueling Mirrors) - 147 - Secret Rare - NM - German - FirstEd - 12,80 EUR",
            productName: String = "Trident Dragion",
            name: String = "Dreiköpfiger Drache",
            language: String = "German",
            rarity: String = "Secret Rare",
            productId: Long = 788591
        ) = Card(
            cardId = cardId,
            completeDescription = completeDescription,
            productName = productName,
            name = name,
            language = language,
            rarity = rarity,
            productId = productId,
        )
    }
}