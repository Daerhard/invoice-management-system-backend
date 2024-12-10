package invoice.management.system.services.invoiceCreation

import invoice.management.system.api.CsvImportApiDelegate
import invoice.management.system.services.csvImport.CSVEntityConverter
import invoice.management.system.services.csvImport.CSVTranslationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ImportService(
    private val csvTranslationService: CSVTranslationService,
    private val csvEntityConverter: CSVEntityConverter
): CsvImportApiDelegate {

    override fun importCSVData(): ResponseEntity<Unit> {
        // Access file in resources/import_files
//        val filePath = this::class.java.classLoader.getResource("import_files/fileName.csv")?.path
//            ?: throw IllegalArgumentException("File not found in resources/import_files!")

        val filePath =
            this::class.java.classLoader.getResource("import_files/Orders-byPaymentDate-2024-10-01_2024-10-31.csv")?.path
                ?: throw IllegalArgumentException("File not found in resources/import_files!")

        val csvOrders = csvTranslationService.translateOrders(filePath)
        csvEntityConverter.convertCSVOrders(csvOrders)

        return ResponseEntity(HttpStatus.CREATED)
    }
}