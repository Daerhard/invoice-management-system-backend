package invoice.management.system.services.csvImport

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import invoice.management.system.services.orderItemDescription.DescriptionDetail
import invoice.management.system.services.orderItemDescription.OrderItemDescriptionService
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import java.io.Reader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CSVTranslationService {

    companion object {
        private val CSV_PARSER = CSVParserBuilder().withSeparator(';').build()
    }

    fun translateOrders(file: Resource): List<CSVOrder> {
        return file.inputStream.use { inputStream ->
            translateCSV(InputStreamReader(inputStream)) { fields -> parseOrder(fields) }
        }
    }

    fun translatePurchases(file: Resource): List<CSVPurchase> {
        return file.inputStream.use { inputStream ->
            translateCSV(InputStreamReader(inputStream)) { fields -> parsePurchase(fields) }
        }
    }

    private fun <T> translateCSV(reader: Reader, parseRow: (Array<String>) -> T): List<T> {
        return CSVReaderBuilder(reader)
            .withCSVParser(CSV_PARSER)
            .build()
            .use { csvReader ->
                csvReader.readAll()
                    .drop(1)
                    .map { fields -> parseRow(fields) }
            }
    }

    private fun parseOrder(fields: Array<String>): CSVOrder {
        val completeDescription = fields[15]
        val splitDescription = completeDescription.split("|").map { it.trim() }
        val localizedProductNames = fields[17].split("|").map { it.trim() }
        val productIds = fields[16].split("|").map { it.trim() }.map(String::toLong)
        val orderProducts = try {
            createOrderProducts(localizedProductNames, productIds, splitDescription)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create order products for order ID: ${fields[0].toLong()}", e)
        }

        val csvOrder = CSVOrder(
            externalOrderId = fields[0].toLong(),
            username = fields[1],
            name = fields[2],
            street = fields[3],
            city = fields[4],
            country = fields[5],
            isProfessional = fields[6].isNotEmpty(),
            vatNumber = fields[7].takeIf { it.isNotEmpty() },
            dateOfPayment = parsePaymentDate(fields[8]),
            articleCount = fields[9].toInt(),
            merchandiseValue = fields[10].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid merchandise value"),
            shipmentCosts = fields[11].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid shipment costs"),
            totalValue = fields[12].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid total value"),
            commission = fields[13].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid commission"),
            currency = fields[14],
            completeDescription = completeDescription,
            splitDescription = splitDescription,
            productIds = productIds,
            localizedProductNames = localizedProductNames,
            orderProducts = orderProducts
        )
        return csvOrder
    }

    private fun parsePurchase(fields: Array<String>): CSVPurchase {
        val completeDescription = fields[15]
        val splitDescription = completeDescription.split("|").map { it.trim() }
        val localizedProductNames = fields[17].split("|").map { it.trim() }
        val productIds = fields[16].split("|").map { it.trim() }.map(String::toLong)
        val orderProducts = try {
            createOrderProducts(localizedProductNames, productIds, splitDescription)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create order products for purchase ID: ${fields[0].toLong()}", e)
        }

        return CSVPurchase(
            externalOrderId = fields[0].toLong(),
            sellerUsername = fields[1],
            dateOfPayment = parsePaymentDate(fields[8]),
            articleCount = fields[9].toInt(),
            merchandiseValue = fields[10].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid merchandise value"),
            shipmentCosts = fields[11].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid shipment costs"),
            trusteeFee = fields[12].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid trustee service fee"),
            totalValue = fields[13].replace(",", ".").toDoubleOrNull() ?: throw IllegalArgumentException("Invalid total value"),
            currency = fields[14],
            orderProducts = orderProducts
        )
    }

    private fun createOrderProducts(
        localizedProductNames: List<String>,
        productIds: List<Long>,
        splitDescription: List<String>
    ): List<OrderProduct> {

        val orderProducts = localizedProductNames.indices.map { index ->
            OrderProduct(
                productIds[index],
                localizedProductNames[index],
                splitDescription[index],
                OrderItemDescriptionService().getDescriptionDetails(splitDescription[index])
            )
        }

        return orderProducts
    }

    fun parsePaymentDate(input: String): LocalDateTime {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val (rawDate, rawTime) = input.trim().split(" ")

        val date = when {
            "-" in rawDate -> {
                val parts = rawDate.split("-")
                if (parts[0].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else rawDate
            }
            "." in rawDate -> {
                val parts = rawDate.split(".")
                if (parts[0].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else rawDate.replace(".", "-")
            }
            else -> rawDate.replace(".", "-")
        }

        val time = rawTime.take(5)

        return LocalDateTime.parse("$date $time", dateTimeFormatter)
    }
}

data class CSVOrder(
    val externalOrderId: Long,
    val username: String,
    val name: String,
    val street: String,
    val city: String,
    val country: String,
    val isProfessional: Boolean?,
    val vatNumber: String?,
    val dateOfPayment: LocalDateTime,
    val articleCount: Int,
    val merchandiseValue: Double,
    val shipmentCosts: Double,
    val totalValue: Double,
    val commission: Double,
    val currency: String,
    val completeDescription: String,
    val splitDescription: List<String>,
    val productIds: List<Long>,
    val localizedProductNames: List<String>,
    val orderProducts: List<OrderProduct> = emptyList()
)

data class CSVPurchase(
    val externalOrderId: Long,
    val sellerUsername: String,
    val dateOfPayment: LocalDateTime,
    val articleCount: Int,
    val merchandiseValue: Double,
    val shipmentCosts: Double,
    val trusteeFee: Double,
    val totalValue: Double,
    val currency: String,
    val orderProducts: List<OrderProduct> = emptyList()
)

data class OrderProduct(
    val productId: Long,
    val localizedName: String,
    val description: String,
    val descriptionDetail: DescriptionDetail
)
