package invoice.management.system.services.invoiceGeneration.zugferdGeneration

import com.neovisionaries.i18n.CountryCode
import invoice.management.system.entities.CardmarketOrder
import invoice.management.system.entities.OrderItem
import invoice.management.system.utils.CountryCodes
import io.konik.zugferd.Invoice
import io.konik.zugferd.entity.*
import io.konik.zugferd.entity.trade.*
import io.konik.zugferd.entity.trade.item.*
import io.konik.zugferd.profile.ConformanceLevel
import io.konik.zugferd.unece.codes.DocumentCode
import io.konik.zugferd.unece.codes.Reference
import io.konik.zugferd.unece.codes.TaxCode
import io.konik.zugferd.unqualified.Amount
import io.konik.zugferd.unqualified.Quantity
import io.konik.zugferd.unqualified.ZfDateFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

/*
@Service
class InvoiceZUGFeRDGenerationService {

    private fun createInvoice(cardmarketOrder: CardmarketOrder): Invoice {

        val invoice = Invoice(ConformanceLevel.BASIC)
        invoice.setHeader(createHeader(invoice, cardmarketOrder))

        val trade = Trade()
        trade.setAgreement()


        setAgreement(invoice, cardmarketOrder)

        trade.setDelivery(Delivery(nextMonth))

        val itemTax = createItemTax()

        trade.addItem(
            createItem()
        )

        trade.setSettlement(
            Settlement().setPaymentReference("20131122-42").setCurrency(EUR)
                .addPaymentMeans(
                    PaymentMeans().setPayerAccount(DebtorFinancialAccount("DE01234.."))
                        .setPayerInstitution(FinancialInstitution("GENO..."))
                )
                .setMonetarySummation(
                    MonetarySummation().setLineTotal(Amount(100, EUR)).setChargeTotal(Amount(0, EUR))
                        .setAllowanceTotal(Amount(0, EUR)).setTaxBasisTotal(Amount(100, EUR))
                        .setTaxTotal(Amount(19, EUR)).setDuePayable(Amount(119, EUR))
                        .setTotalPrepaid(Amount(0, EUR)).setGrandTotal(Amount(119, EUR))
                )
        )

        invoice.setTrade(trade)

        return invoice
    }

    private fun createHeader(invoice: Invoice, cardmarketOrder: CardmarketOrder): Header {
        val todayZfDate= ZfDateFactory.create(LocalDate.now().toString())
        val invoiceNumber = cardmarketOrder.externalOrderId.toString()

        return Header()
            .setInvoiceNumber(invoiceNumber)
            .setCode(DocumentCode._380)
            .setIssued(todayZfDate)
            .setName("Rechnung")
    }

    private fun createAgreement(invoice: Invoice, cardmarketOrder: CardmarketOrder): Agreement {
        val seller = Seller()
        val buyer = cardmarketOrder.customer

        val agreement = Agreement()

        agreement.setSeller(
            TradeParty().setName(seller.fullName)
                .setAddress(Address(seller.zipCode, seller.street, seller.city, seller.country))
                .addTaxRegistrations(TaxRegistration(seller.vatNumber, Reference.FC))
        )

        val countryCode = CountryCodes.valueOf(buyer.country).countryCode

        if(buyer.vatNumber != null) {
        agreement.setBuyer(
            TradeParty().setName(buyer.fullName)
                .setAddress(Address(buyer.city, "Domkloster 4", "Köln", CountryCodes.valueOf(buyer.country).countryCode)))
                .addTaxRegistrations(TaxRegistration("DE123...", FC))
        )
        } else {
            agreement.setBuyer(
                TradeParty().setName(buyer.fullName).setAddress(Address(buyer.city, "Domkloster 4", "Köln", DE)))
        }

        return agreement
    }

    private fun createItem(orderItem: OrderItem) : Item{
        val productName = orderItem.card.productName

        val item = Item().setProduct(Product().setName(productName))
            .setAgreement(
                SpecifiedAgreement().setGrossPrice(GrossPrice(Amount(100, EUR)))
                    .setNetPrice(Price(Amount(100, EUR)))
            )
            .setSettlement(SpecifiedSettlement().addTradeTax(itemTax))
            .setDelivery(SpecifiedDelivery(Quantity(1, UNIT)))

        return item
    }

    private fun createItemTax(): ItemTax {
        val itemTax = ItemTax()
        itemTax.setPercentage(BigDecimal.valueOf(19))
        itemTax.setType(TaxCode.VAT)

        return itemTax
    }
}

// toDo: remove after authentication is implemented. Seller data should be fetched from the database
data class Seller(
    val fullName: String = "Daniel Erhard",
    val zipCode: String = "86916",
    val city: String = "Kaufering",
    val street: String = "Thomas-Morus-Str. 2",
    val country: CountryCode = CountryCode.DE,
    val vatNumber: String = "DE123456789"
)


 */
