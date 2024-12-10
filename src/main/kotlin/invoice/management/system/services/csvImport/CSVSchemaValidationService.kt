package invoice.management.system.services.csvImport

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import java.io.Reader

@Service
class CSVSchemaValidationService {

    companion object {
        private val EXPECTED_SCHEMA = listOf(
            CSVSchema("OrderID", 0),
            CSVSchema("Username", 1),
            CSVSchema("Name", 2),
            CSVSchema("Street", 3),
            CSVSchema("City", 4),
            CSVSchema("Country", 5),
            CSVSchema("Is Professional", 6),
            CSVSchema("VAT Number", 7),
            CSVSchema("Date of Payment", 8),
            CSVSchema("Article Count", 9),
            CSVSchema("Merchandise Value", 10),
            CSVSchema("Shipment Costs", 11),
            CSVSchema("Total Value", 12),
            CSVSchema("Commission", 13),
            CSVSchema("Currency", 14),
            CSVSchema("Description", 15),
            CSVSchema("Product ID", 16),
            CSVSchema("Localized Product Name", 17)
        )
    }

    fun validateSchema(file: Resource): List<String> {
        val schema = file.inputStream.use { inputStream ->
            extractSchema(InputStreamReader(inputStream))
        }

        val errors = mutableListOf<String>()
        if (schema.size != EXPECTED_SCHEMA.size) {
            errors.add("Column count mismatch: expected ${EXPECTED_SCHEMA.size}, found ${schema.size}.")
        } else {
            schema.forEachIndexed { index, columnName ->
                val expectedColumn = EXPECTED_SCHEMA.getOrNull(index)
                if (expectedColumn == null || columnName != expectedColumn.name) {
                    errors.add("Column mismatch at index $index: expected '${expectedColumn?.name}', found '$columnName'.")
                }
            }
        }

        return errors
    }

    private fun extractSchema(reader: Reader): Array<out String> {
        val csvParser = CSVParserBuilder().withSeparator(';').build()
        return CSVReaderBuilder(reader)
            .withCSVParser(csvParser)
            .build()
            .use { csvReader ->
                csvReader.readAll()
                    .firstOrNull() ?: throw IllegalArgumentException("CSV file is empty or missing headers.")
            }
    }
}

data class CSVSchema(
    val name: String,
    val index: Int
)
