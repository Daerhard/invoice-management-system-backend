package invoice.management.system.zugferd.services

import invoice.management.system.entities.CardmarketOrder
import mu.KotlinLogging
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

private val logger = KotlinLogging.logger {}

@Service
class ZugferdGenerationService {

    // Seller data – replace with database-backed user data once authentication is implemented
    private val sellerName = "Daniel Erhard"
    private val sellerStreet = "Thomas-Morus-Str. 2"
    private val sellerZip = "86916"
    private val sellerCity = "Kaufering"
    private val sellerCountry = "DE"
    private val sellerTaxId = "DE123456789"

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
