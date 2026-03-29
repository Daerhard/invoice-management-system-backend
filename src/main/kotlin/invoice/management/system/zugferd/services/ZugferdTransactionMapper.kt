package invoice.management.system.zugferd.services

import invoice.management.system.entities.CardmarketOrder
import org.mustangproject.Invoice
import org.mustangproject.Item
import org.mustangproject.Product
import org.mustangproject.TradeParty
import java.math.BigDecimal
import java.util.Date

object ZugferdTransactionMapper {

    fun map(
        order: CardmarketOrder,
        sellerName: String,
        sellerStreet: String,
        sellerZip: String,
        sellerCity: String,
        sellerCountry: String,
        sellerTaxId: String,
    ): Invoice {
        val seller = TradeParty(sellerName, sellerStreet, sellerZip, sellerCity, sellerCountry)
            .addTaxID(sellerTaxId)

        val buyer = order.customer
        val buyerCountryCode = resolveCountryCode(buyer.country)
        val buyerParty = TradeParty(buyer.fullName, buyer.street, "", buyer.city, buyerCountryCode)
        if (buyer.vatNumber != null) {
            buyerParty.addVATID(buyer.vatNumber)
        }

        val issueDate = Date()

        val invoice = Invoice()
        invoice.setNumber(order.externalOrderId.toString())
        invoice.setIssueDate(issueDate)
        invoice.setDeliveryDate(issueDate)
        invoice.setSender(seller)
        invoice.setRecipient(buyerParty)
        invoice.setCurrency(order.currency)
        invoice.setPaymentTermDescription("Zahlbar sofort ohne Abzug")
        invoice.setOwnTaxID(sellerTaxId)

        order.orderItems.forEach { orderItem ->
            val product = Product(
                orderItem.card.productName,
                "",
                "C62",
                BigDecimal.ZERO,
            )
            product.setTaxCategoryCode("Z")

            val item = Item()
            item.setProduct(product)
            item.setPrice(BigDecimal.valueOf(orderItem.price))
            item.setQuantity(BigDecimal.valueOf(orderItem.count.toLong()))

            invoice.addItem(item)
        }

        return invoice
    }

    private fun resolveCountryCode(country: String): String {
        return when (country.uppercase()) {
            "GERMANY" -> "DE"
            "AUSTRIA" -> "AT"
            "SWITZERLAND" -> "CH"
            "FRANCE" -> "FR"
            "NETHERLANDS" -> "NL"
            "BELGIUM" -> "BE"
            "SPAIN" -> "ES"
            "ITALY" -> "IT"
            "POLAND" -> "PL"
            "CZECH REPUBLIC" -> "CZ"
            "UK", "GREAT BRITAIN" -> "GB"
            "USA", "UNITED STATES" -> "US"
            else -> if (country.length == 2) country.uppercase() else "DE"
        }
    }
}
