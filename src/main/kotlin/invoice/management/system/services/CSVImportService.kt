package invoice.management.system.services

import com.opencsv.CSVParser
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class CSVImportService(
    private val csvTranslationService: CSVTranslationService,
    private val CSVEntityMapper: CSVEntityMapper,
) {

    @PostConstruct
    fun onStartup() {

        // Access file in resources/import_files
//        val filePath = this::class.java.classLoader.getResource("import_files/fileName.csv")?.path
//            ?: throw IllegalArgumentException("File not found in resources/import_files!")

        val filePath =
            this::class.java.classLoader.getResource("import_files/Orders-byPaymentDate-2024-10-01_2024-10-31.csv")?.path
                ?: throw IllegalArgumentException("File not found in resources/import_files!")

        importCSVData( filePath )
    }

    fun importCSVData( filePath: String ) {

        val orders = csvTranslationService.translateOrders(filePath)

        val customer = CSVEntityMapper.convertToCustomers(orders)

        val card = CSVEntityMapper.convertToCards(orders)


    }

}