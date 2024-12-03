package invoice.management.system.services

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class CSVImportService(
    private val csvTranslationService: CSVTranslationService,
    private val csvEntityMapper: CSVEntityMapper,
    private val databaseImportService: DatabaseImportService
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

        val customers = csvEntityMapper.convertToCustomers(orders)
        databaseImportService.saveCustomers(customers)

        val cards = csvEntityMapper.convertToCards(orders)
        databaseImportService.saveCards(cards)

        val purchases = csvEntityMapper.convertToPurchases(orders, customers, cards)
        databaseImportService.savePurchases(purchases)

    }

}