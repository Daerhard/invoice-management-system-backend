package invoice.management.system.services.csvImport

import invoice.management.system.entities.*
import invoice.management.system.repositories.CardRepository
import invoice.management.system.repositories.CustomerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CSVEntityConverter(
    private val customerRepository: CustomerRepository,
    private val cardRepository: CardRepository
) {

    @Transactional
    fun convertCSVOrders(csvOrder: List<CSVOrder>) {

        csvOrder.forEach { csvOrder ->

            val customer = when (val existingCustomer = customerRepository.findByUserName(csvOrder.username)) {
                null -> createNewCustomer(csvOrder)
                else -> updateExistingCustomer(existingCustomer, csvOrder)
            }

            val order = createOrder(csvOrder, customer)
            val orderItems = createOrderItems(order, csvOrder)
            order.orderItems.addAll(orderItems)
        }

    }

    private fun createNewCustomer(csvOrder: CSVOrder): Customer {
        return customerRepository.save(
            Customer(
                userName = csvOrder.username,
                fullName = csvOrder.name,
                street = csvOrder.street,
                city = csvOrder.city,
                country = csvOrder.country,
                isProfessional = csvOrder.isProfessional ?: false,
            )
        )
    }

    private fun updateExistingCustomer(customer: Customer, csvOrder: CSVOrder): Customer {
        return customer.apply {
            fullName = csvOrder.name
            street = csvOrder.street
            city = csvOrder.city
            country = csvOrder.country
            isProfessional = csvOrder.isProfessional ?: isProfessional
        }
    }

    private fun createOrder(
        csvOrder: CSVOrder,
        customer: Customer,
    ): CardmarketOrder {
        return CardmarketOrder(
            customer = customer,
            externalOrderId = csvOrder.orderId,
            dateOfPayment = csvOrder.dateOfPayment,
            articleCount = csvOrder.articleCount,
            merchandiseValue = csvOrder.merchandiseValue,
            shipmentCost = csvOrder.shipmentCosts,
            totalValue = csvOrder.totalValue,
            commission = csvOrder.commission,
            currency = csvOrder.currency,
        )
    }


    private fun createOrderItems(
        cardmarketOrder: CardmarketOrder,
        csvOrder: CSVOrder
    ): List<OrderItem> {
        return csvOrder.orderProducts.map { orderProduct ->
            val descriptionDetail = orderProduct.descriptionDetail

            OrderItem(
                cardmarketOrder = cardmarketOrder,
                count = csvOrder.articleCount,
                condition = descriptionDetail.condition,
                price = descriptionDetail.price,
                card = cardRepository.findByIdOrNull(
                    CardId(
                        descriptionDetail.konamiSet,
                        descriptionDetail.productNumber
                    )
                ) ?: createCard(orderProduct)
            )
        }
    }

    private fun createCard(orderProduct: OrderProduct): Card {
        val descriptionDetail = orderProduct.descriptionDetail

        return cardRepository.save(
            Card(
                CardId(
                    konamiSet = descriptionDetail.konamiSet,
                    number = descriptionDetail.productNumber
                ),
                completeDescription = orderProduct.description,
                productName = descriptionDetail.productName,
                name = orderProduct.localizedName,
                language = descriptionDetail.language,
                rarity = descriptionDetail.rarity,
                isFirstEdition = descriptionDetail.isFirstEdition,
                productId = orderProduct.productId
            )
        )
    }
}
