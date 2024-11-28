package invoice.management.system.services

import com.opencsv.CSVParser
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import org.springframework.stereotype.Service
import com.opencsv.CSVReaderBuilder
import jakarta.annotation.PostConstruct
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class TranslationService(
    private val databaseImportService: DatabaseImportService,
    private val entityConversionService: EntityConversionService
) {

    @PostConstruct
    fun onStartup() {
        // Access file in resources/import_files
//        val filePath = this::class.java.classLoader.getResource("import_files/fileName.csv")?.path
//            ?: throw IllegalArgumentException("File not found in resources/import_files!")

        val filePath =
            this::class.java.classLoader.getResource("import_files/Orders-byPaymentDate-2024-10-01_2024-10-31.csv")?.path
                ?: throw IllegalArgumentException("File not found in resources/import_files!")

        val csvParser: CSVParser = CSVParserBuilder().withSeparator(';').build()
        val csvReader = CSVReaderBuilder(FileReader(filePath)).withCSVParser(csvParser).build()
        val orders = translateOrders(csvReader)

        val customers = entityConversionService.convertToCustomers(orders)
        databaseImportService.saveCustomers(customers)

        val cards = entityConversionService.convertToCards(orders)
        databaseImportService.saveCards(cards)

    }

    fun translateOrders(csvReader: CSVReader): List<Order> {
        val orders = mutableListOf<Order>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        csvReader.use { reader ->
            val rows = reader.readAll()
            rows.drop(1).forEach { fields ->
                orders.add(
                    Order(
                        orderId = fields[0].toLong(),
                        username = fields[1],
                        name = fields[2],
                        street = fields[3],
                        city = fields[4],
                        country = fields[5],
                        isProfessional = fields[6].takeIf { it.isNotEmpty() }?.toBoolean(),
                        vatNumber = fields[7].takeIf { it.isNotEmpty() },
                        dateOfPayment = LocalDateTime.parse(fields[8], formatter),
                        articleCount = fields[9].toInt(),
                        merchandiseValue = fields[10].replace(",", ".").toDouble(),
                        shipmentCosts = fields[11].replace(",", ".").toDouble(),
                        totalValue = fields[12].replace(",", ".").toDouble(),
                        commission = fields[13].replace(",", ".").toDouble(),
                        currency = fields[14],
                        completeDescription = fields[15],
                        splitDescription = fields[15].split("|").map { it.trim() },
                        productIds = fields[16].split("|").map { it.trim().toLong() },
                        localizedProductNames = fields[17].split("|").map { it.trim() }
                    )
                )

            }
        }
        return orders
    }
}

data class Order(
    val orderId: Long,                  // 0: Unique Order ID
    val username: String,               // 1: Username
    val name: String,                   // 2: Customer's Full Name
    val street: String,                 // 3: Street Address
    val city: String,                   // 4: City
    val country: String,                // 5: Country
    val isProfessional: Boolean?,       // 6: Indicates if the customer is a professional (nullable)
    val vatNumber: String?,             // 7: VAT Number (nullable)
    val dateOfPayment: LocalDateTime,   // 8: Date of Payment
    val articleCount: Int,              // 9: Count of Articles
    val merchandiseValue: Double,       // 10: Value of Merchandise
    val shipmentCosts: Double,          // 11: Shipment Costs
    val totalValue: Double,             // 12: Total Order Value
    val commission: Double,             // 13: Commission Fee
    val currency: String,               // 14: Currency (e.g., EUR)
    val completeDescription: String,    // 15: Detailed Description of Products
    val splitDescription: List<String>, // 16: Split Description of Products
    val productIds: List<Long>,         // 17: List of Product IDs
    val localizedProductNames: List<String> // 18: List of Localized Product Names
)
