package invoice.management.system.services.csvImport

import invoice.management.system.api.CardmarketPurchasesApiDelegate
import invoice.management.system.model.CardmarketPurchaseDto
import invoice.management.system.model.CreateRefund400ResponseDto
import invoice.management.system.model.ResponseMessageDto
import invoice.management.system.repositories.CardmarketPurchaseRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CardmarketPurchaseService(
    private val csvTranslationService: CSVTranslationService,
    private val csvEntityConverter: CSVEntityConverter,
    private val csvSchemaValidationService: CSVSchemaValidationService,
    private val cardmarketPurchaseRepository: CardmarketPurchaseRepository,
) : CardmarketPurchasesApiDelegate {

    override fun importCSVPurchaseData(file: Resource?): ResponseEntity<CreateRefund400ResponseDto> {
        if (file == null) {
            return ResponseEntity(CreateRefund400ResponseDto("No file provided"), HttpStatus.BAD_REQUEST)
        }

        val validationErrors = csvSchemaValidationService.validatePurchaseSchema(file)
        if (validationErrors.isNotEmpty()) {
            return ResponseEntity(
                CreateRefund400ResponseDto("CSV schema validation failed:$validationErrors"),
                HttpStatus.BAD_REQUEST
            )
        }

        return try {
            val csvPurchases = csvTranslationService.translatePurchases(file)
            csvEntityConverter.convertCSVPurchases(csvPurchases)
            ResponseEntity(CreateRefund400ResponseDto("CSV file import was successful"), HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity(CreateRefund400ResponseDto(ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    @Transactional(readOnly = true)
    override fun getPurchases(): ResponseEntity<List<CardmarketPurchaseDto>> {
        val purchases = cardmarketPurchaseRepository.findAll()
        return ResponseEntity(purchases.map { it.toDto() }, HttpStatus.OK)
    }

    @Transactional(readOnly = true)
    override fun getPurchasesByUserName(userName: String): ResponseEntity<List<CardmarketPurchaseDto>> {
        val purchases = cardmarketPurchaseRepository.findBySellerUserName(userName)
        return ResponseEntity(purchases.map { it.toDto() }, HttpStatus.OK)
    }
}
