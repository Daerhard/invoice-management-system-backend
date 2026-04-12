package invoice.management.system.services.csvImport

import invoice.management.system.entities.*
import invoice.management.system.repositories.CardRepository
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.CardmarketPurchaseRepository
import invoice.management.system.repositories.CustomerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CSVEntityConverter(
    private val customerRepository: CustomerRepository,
    private val cardRepository: CardRepository,
    private val cardMarketOrderRepository: CardmarketOrderRepository,
    private val cardmarketPurchaseRepository: CardmarketPurchaseRepository,
) {

    @Transactional
    fun convertCSVOrders(csvOrder: List<CSVOrder>) {
        csvOrder.forEach { csvOrder ->
            val customer = when (val existingCustomer = customerRepository.findByUserName(csvOrder.username)) {
                null -> createNewCustomer(csvOrder)
                else -> updateExistingCustomer(existingCustomer, csvOrder)
            }
            cardMarketOrderRepository.findByExternalOrderId(csvOrder.externalOrderId) ?: createNewCardmarketOrder(
                csvOrder,
                customer
            )
        }
    }

    @Transactional
    fun convertCSVPurchases(csvPurchases: List<CSVPurchase>) {
        csvPurchases.forEach { csvPurchase ->
            cardmarketPurchaseRepository.findByExternalOrderId(csvPurchase.externalOrderId)
                ?: createNewCardmarketPurchase(csvPurchase)
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
            isProfessional = csvOrder.isProfessional ?: false
        }
    }

    private fun createNewCardmarketOrder(
        csvOrder: CSVOrder,
        customer: Customer,
    ) {
        val cardMarketOrder = CardmarketOrder(
            customer = customer,
            externalOrderId = csvOrder.externalOrderId,
            dateOfPayment = csvOrder.dateOfPayment.toLocalDate(),
            articleCount = csvOrder.articleCount,
            merchandiseValue = csvOrder.merchandiseValue,
            shipmentCost = csvOrder.shipmentCosts,
            totalValue = csvOrder.totalValue,
            commission = csvOrder.commission,
            currency = csvOrder.currency,
        )

        val orderItems = createOrderItems(cardMarketOrder, csvOrder)
        cardMarketOrder.orderItems.addAll(orderItems)
        cardMarketOrderRepository.save(cardMarketOrder)
    }

    private fun createNewCardmarketPurchase(csvPurchase: CSVPurchase) {
        val purchase = CardmarketPurchase(
            sellerUserName = csvPurchase.sellerUsername,
            externalOrderId = csvPurchase.externalOrderId,
            dateOfPayment = csvPurchase.dateOfPayment.toLocalDate(),
            articleCount = csvPurchase.articleCount,
            merchandiseValue = csvPurchase.merchandiseValue,
            shipmentCost = csvPurchase.shipmentCosts,
            trusteeFee = csvPurchase.trusteeFee,
            totalValue = csvPurchase.totalValue,
            currency = csvPurchase.currency,
        )

        val purchaseItems = createPurchaseItems(purchase, csvPurchase)
        purchase.purchaseItems.addAll(purchaseItems)
        cardmarketPurchaseRepository.save(purchase)
    }

    private fun createOrderItems(
        cardmarketOrder: CardmarketOrder,
        csvOrder: CSVOrder
    ): List<OrderItem> {
        return csvOrder.orderProducts.map { orderProduct ->
            val descriptionDetail = orderProduct.descriptionDetail

            OrderItem(
                cardmarketOrder = cardmarketOrder,
                count = descriptionDetail.articleCount,
                condition = descriptionDetail.condition,
                price = descriptionDetail.price,
                isFirstEdition = descriptionDetail.isFirstEdition,
                card = cardRepository.findByIdOrNull(
                    CardId(
                        descriptionDetail.konamiSet,
                        descriptionDetail.productNumber
                    )
                ) ?: createCard(orderProduct)
            )
        }
    }

    private fun createPurchaseItems(
        purchase: CardmarketPurchase,
        csvPurchase: CSVPurchase
    ): List<PurchaseItem> {
        return csvPurchase.orderProducts.map { orderProduct ->
            val descriptionDetail = orderProduct.descriptionDetail

            PurchaseItem(
                cardmarketPurchase = purchase,
                count = descriptionDetail.articleCount,
                condition = descriptionDetail.condition,
                price = descriptionDetail.price,
                isFirstEdition = descriptionDetail.isFirstEdition,
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
                    konamiNumber = descriptionDetail.productNumber
                ),
                completeDescription = orderProduct.description,
                productName = descriptionDetail.productName,
                name = orderProduct.localizedName,
                language = descriptionDetail.language,
                rarity = descriptionDetail.rarity,
                productId = orderProduct.productId
            )
        )
    }
}
