package invoice.management.system.services.csvImport

import invoice.management.system.api.CSVImportApiDelegate
import invoice.management.system.model.ResponseMessageDto
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class CSVImportService(
    private val csvTranslationService: CSVTranslationService,
    private val csvEntityConverter: CSVEntityConverter,
    private val csvSchemaValidationService: CSVSchemaValidationService
) : CSVImportApiDelegate {

    override fun importCSVData(file: Resource?): ResponseEntity<ResponseMessageDto> {

        if (file == null) {
            return ResponseEntity(ResponseMessageDto("No file provided"), HttpStatus.BAD_REQUEST)
        }

        val validationErrors = csvSchemaValidationService.validateSchema(file)
        if (validationErrors.isNotEmpty()) {
            return ResponseEntity(
                ResponseMessageDto(
                    "CSV schema validation failed:$validationErrors"
                ),
                HttpStatus.BAD_REQUEST
            )
        }

        return try {
            val csvOrders = csvTranslationService.translateOrders(file)
            csvEntityConverter.convertCSVOrders(csvOrders)
            ResponseEntity(ResponseMessageDto("CSV file import was successful"), HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity(ResponseMessageDto(
                ex.message
            ),HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
