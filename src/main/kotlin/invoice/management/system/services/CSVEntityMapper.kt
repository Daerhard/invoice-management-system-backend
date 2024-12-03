package invoice.management.system.services

import invoice.management.system.entities.*
import org.springframework.stereotype.Service

@Service
class CSVEntityMapper {

    fun convertToCustomers(orders: List<Order>): List<Customer> {
        return orders.map { order ->
            Customer(
                userName = order.username,
                fullName = order.name,
                street = order.street,
                city = order.city,
                country = order.country,
                isProfessional = order.isProfessional ?: false
            )
        }
    }

    fun convertToCards(orders: List<Order>): List<Card> {
        return orders.flatMap { order ->
            order.orderProducts.map { product ->
                createCard(product)
            }
        }
    }

    private fun createCard(orderProduct: OrderProduct): Card {
        val descriptionDetail = orderProduct.descriptionDetail

        return Card(
            id = CardId(
                konamiSet = descriptionDetail.konamiSet,
                number = descriptionDetail.productNumber
            ),
            completeDescription = orderProduct.description,
            productName = descriptionDetail.productName,
            name = orderProduct.localizedName,
            language = descriptionDetail.language,
            condition = descriptionDetail.condition,
            rarity = descriptionDetail.rarity,
            isFirstEdition = descriptionDetail.isFirstEdition,
            productId = orderProduct.productId
        )
    }

    fun convertToPurchases(orders: List<Order>, customers: List<Customer>, cards: List<Card>): List<Purchase> {
        return orders.map { order ->
            val customer = customers.find { it.userName == order.username }
                ?: throw IllegalArgumentException("Customer not found for username: ${order.username}")

            Purchase(
                customer = customer,
                orderId = order.orderId,
                dateOfPayment = order.dateOfPayment,
                articleCount = order.articleCount,
                merchandiseValue = order.merchandiseValue,
                shipmentCost = order.shipmentCosts,
                totalValue = order.totalValue,
                commission = order.commission,
                currency = order.currency,
                purchaseItems = createPurchaseItems(order, cards)
            )
        }
    }

    private fun createPurchaseItems(order: Order, cards: List<Card>): List<PurchaseItem> {
        return order.orderProducts.map { orderProduct ->
            val card = cards.find {
                it.id == CardId(
                    konamiSet = orderProduct.descriptionDetail.konamiSet,
                    number = orderProduct.descriptionDetail.productNumber
                )
            } ?: throw IllegalArgumentException("Card not found for product ID: ${orderProduct.productId}")

            PurchaseItem(
                id = null,
                purchaseId = order.orderId,
                count = order.articleCount,
                condition = orderProduct.descriptionDetail.condition,
                price = orderProduct.descriptionDetail.price,
                card = card
            )
        }
    }
}
