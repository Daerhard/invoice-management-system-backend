package invoice.management.system.services.purchaseInvoice

import invoice.management.system.api.PurchaseInvoicesApiDelegate
import invoice.management.system.entities.PurchaseInvoice
import invoice.management.system.model.PurchaseInvoiceDto
import invoice.management.system.repositories.PurchaseInvoiceRepository
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class PurchaseInvoiceService(
    private val purchaseInvoiceRepository: PurchaseInvoiceRepository
) : PurchaseInvoicesApiDelegate {

    override fun createPurchaseInvoice(
        productName: String,
        amount: Int,
        price: Double,
        invoiceDate: LocalDate,
        pdf: Resource?
    ): ResponseEntity<PurchaseInvoiceDto> {

        if (productName.isBlank()) {
            return ResponseEntity.badRequest().build()
        }

        if (amount <= 0) {
            return ResponseEntity.badRequest().build()
        }

        if (price <= 0.0) {
            return ResponseEntity.badRequest().build()
        }

        if (pdf == null) {
            return ResponseEntity.badRequest().build()
        }

        val pdfBytes = try {
            pdf.inputStream.readBytes()
        } catch (ex: Exception) {
            return ResponseEntity.internalServerError().build()
        }

        if (pdfBytes.size < 4 ||
            !pdfBytes.copyOfRange(0, 4)
                .contentEquals(byteArrayOf(0x25, 0x50, 0x44, 0x46))
        ) {
            return ResponseEntity.badRequest().build()
        }

        val now = LocalDateTime.now()

        val entity = PurchaseInvoice(
            productName = productName,
            amount = amount,
            price = BigDecimal.valueOf(price),
            invoiceDate = invoiceDate,
            pdfData = pdfBytes,
            createdAt = now,
            updatedAt = now
        )

        val saved = purchaseInvoiceRepository.save(entity)
        return ResponseEntity(saved.toDto(), HttpStatus.CREATED)
    }

    private fun PurchaseInvoice.toDto(): PurchaseInvoiceDto =
        PurchaseInvoiceDto(
            id = id,
            productName = productName,
            amount = amount,
            price = price.toDouble(),
            invoiceDate = invoiceDate,
            createdAt = OffsetDateTime.of(createdAt, ZoneOffset.UTC),
            updatedAt = OffsetDateTime.of(updatedAt, ZoneOffset.UTC)
        )
}
