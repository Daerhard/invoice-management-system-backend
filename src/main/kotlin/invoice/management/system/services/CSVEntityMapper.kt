package invoice.management.system.services

import invoice.management.system.entities.Card
import invoice.management.system.entities.CardId
import invoice.management.system.entities.Customer
import org.springframework.stereotype.Service

@Service
class EntityConversionService {

    fun convertToCustomers(orders: List<Order>): List<Customer> {
        val customers = mutableListOf<Customer>()

        orders.forEach { order ->
            customers.add(
                Customer(
                    userName = order.username,
                    fullName = order.name,
                    street = order.street,
                    city = order.city,
                    country = order.country,
                    isProfessional = order.isProfessional ?: false,
                )
            )
        }
        return customers
    }

    fun convertToCards(orders: List<Order>): List<Card> {
        val cards = mutableListOf<Card>()

        orders.forEach { orderProduct ->
            val orderOrderProducts =
                orderProduct.localizedProductNames.zip(orderProduct.productIds).zip(orderProduct.splitDescription) { (name, id), desc ->
                    OrderProduct(productId = id, localizedName = name, description = desc)
                }

            orderOrderProducts.forEach { product ->
                cards.add(
                    createCard(product)
                )
            }

        }
        return cards
    }

    private fun createCard(orderProduct: OrderProduct): Card {
        val description = checkDescription(orderProduct.description)

        /*
        if(description.split("(")[0].contains(" - ")) {

            val test2 = description.split(" (")

            val test = description.split(" - ")
        }

         */


        val splitDescription = description.split(" - ")

        val productTitle = convertProductTitle(splitDescription)

        return Card(
            id = CardId(
                konamiSet = productTitle.konamiSet,
                number = splitDescription[1].toInt()
            ),
            completeDescription = orderProduct.description,
            productName = productTitle.cardName,
            name = orderProduct.localizedName,
            language = splitDescription[4],
            condition = splitDescription[3],
            rarity = splitDescription[2],
            isFirstEdition = splitDescription[5].toBoolean(),
            productId = orderProduct.productId
        )
    }

    private fun checkDescription(description: String): String{
        val standardRegex = Regex("""^\d+x\s.+?\s\([^)]+\)\s-\s\d+\s-\s.+?\s-\s.+?\s-\s.+?\s-\s\d+,\d+\s[A-Z]{3}$""")
        val differentCardVersionRegex = Regex(
            """^\d+x\s.+?\s\([^)]+\)\s\([^)]+\)\s-\s\d+\s-\s.+?\s-\s.+?\s-\s.+?\s-\s\d+,\d+\s[A-Z]{3}$"""
        )

        when{
            differentCardVersionRegex.matches(description) -> {
                val regex = Regex("""\s\([^)]+\)""")

                return description.replaceFirst(regex, "").trim()
            }
             standardRegex.matches(description) -> {
                 return description
             }
            else -> {
                throw IllegalArgumentException("Description: $description does not match format")
            }
        }
    }

    private fun convertProductTitle(productTitleString: List<String>): ProductTitle {
        val regex = Regex("""^\d+x\s""")
        val splitProductTitle = productTitleString[0].split(" (")

        return ProductTitle(
            splitProductTitle[0].replace(regex, ""),
            splitProductTitle[1].trimEnd(')'))
    }
}

data class ProductTitle(
    var cardName: String,
    var konamiSet: String
)

data class OrderProduct(
    val productId: Long,
    val localizedName: String,
    val description: String
)