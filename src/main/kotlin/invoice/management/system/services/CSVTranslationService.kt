package invoice.management.system.services

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.springframework.stereotype.Service
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CSVTranslationService {

    companion object {
        private val CSV_PARSER = CSVParserBuilder().withSeparator(';').build()
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun translateOrders(filePath: String): List<Order> {
        return CSVReaderBuilder(FileReader(filePath))
            .withCSVParser(CSV_PARSER)
            .build()
            .use { reader ->
                reader.readAll()
                    .drop(1) // Skip header row
                    .map { fields -> parseOrder(fields) }
            }
    }

    private fun parseOrder(fields: Array<String>): Order {
        val completeDescription = fields[15]
        val splitDescription = splitField(completeDescription)
        val localizedProductNames = splitField(fields[17])
        val productIds = splitField(fields[16]).map(String::toLong)

        return Order(
            orderId = fields[0].toLong(),
            username = fields[1],
            name = fields[2],
            street = fields[3],
            city = fields[4],
            country = fields[5],
            isProfessional = fields[6].takeIf { it.isNotEmpty() }?.toBoolean(),
            vatNumber = fields[7].takeIf { it.isNotEmpty() },
            dateOfPayment = LocalDateTime.parse(fields[8], DATE_FORMATTER),
            articleCount = fields[9].toInt(),
            merchandiseValue = fields[10].toDoubleOrNull() ?: 0.0,
            shipmentCosts = fields[11].toDoubleOrNull() ?: 0.0,
            totalValue = fields[12].toDoubleOrNull() ?: 0.0,
            commission = fields[13].toDoubleOrNull() ?: 0.0,
            currency = fields[14],
            completeDescription = completeDescription,
            splitDescription = splitDescription,
            productIds = productIds,
            localizedProductNames = localizedProductNames,
            orderProducts = createOrderProducts(localizedProductNames, productIds, splitDescription)
        )
    }

    private fun splitField(field: String): List<String> {
        return field.split("|").map { it.trim() }
    }

    private fun createOrderProducts(
        localizedProductNames: List<String>,
        productIds: List<Long>,
        splitDescription: List<String>
    ): List<OrderProduct> {
        return localizedProductNames.zip(productIds).zip(splitDescription) { (name, id), description ->
            OrderProduct(id, name, description, ProductDescriptionService().convertDescription(description))
        }
    }
}

data class Order(
    val orderId: Long,
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

data class OrderProduct(
    val productId: Long,
    val localizedName: String,
    val description: String,
    val descriptionDetail: DescriptionDetail
)
