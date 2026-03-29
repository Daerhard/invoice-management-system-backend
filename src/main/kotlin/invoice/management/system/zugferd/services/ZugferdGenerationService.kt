package invoice.management.system.zugferd.services

import invoice.management.system.entities.CardmarketOrder
import mu.KotlinLogging
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

private val logger = KotlinLogging.logger {}

@Service
class ZugferdGenerationService(
    @Value("\${seller.name}") private val sellerName: String,
    @Value("\${seller.street}") private val sellerStreet: String,
    @Value("\${seller.zip}") private val sellerZip: String,
    @Value("\${seller.city}") private val sellerCity: String,
    @Value("\${seller.country}") private val sellerCountry: String,
    @Value("\${seller.tax-id}") private val sellerTaxId: String,
) {

    fun generateZugferdInvoice(cardmarketOrder: CardmarketOrder, pdfBytes: ByteArray): ByteArray {
        logger.info { "Generating ZUGFeRD invoice for order ${cardmarketOrder.externalOrderId}" }

        val transaction = ZugferdTransactionMapper.map(
            order = cardmarketOrder,
            sellerName = sellerName,
            sellerStreet = sellerStreet,
            sellerZip = sellerZip,
            sellerCity = sellerCity,
            sellerCountry = sellerCountry,
            sellerTaxId = sellerTaxId,
        )

        val exporter = ZUGFeRDExporterFromA3()
        exporter.ignorePDFAErrors()
        exporter.load(pdfBytes)
        exporter.setProducer("Invoice Management System")
        exporter.setCreator("Invoice Management System")
        exporter.setTransaction(transaction)

        val baos = ByteArrayOutputStream()
        exporter.export(baos)

        logger.info { "ZUGFeRD invoice generated for order ${cardmarketOrder.externalOrderId}" }
        return baos.toByteArray()
    }
}
