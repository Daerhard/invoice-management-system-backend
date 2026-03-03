package invoice.management.system.services.purchaseInvoice

import invoice.management.system.api.PurchaseInvoicesApiDelegate
import invoice.management.system.entities.PurchaseInvoice
import invoice.management.system.model.PurchaseInvoiceResponseDto
import invoice.management.system.repositories.PurchaseInvoiceRepository
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
    ): ResponseEntity<PurchaseInvoiceResponseDto> {

        if (productName.isBlank()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (amount <= 0) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (price <= 0.0) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        if (pdf == null) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val multipartFile = pdf as? MultipartFile
            ?: return ResponseEntity(HttpStatus.BAD_REQUEST)

        val contentType = multipartFile.contentType
        if (contentType == null || !contentType.equals("application/pdf", ignoreCase = true)) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }

        val pdfBytes = try {
            multipartFile.bytes
        } catch (ex: Exception) {
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
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

    override fun getPurchaseInvoiceById(id: Long): ResponseEntity<PurchaseInvoiceResponseDto> {
        val invoice = purchaseInvoiceRepository.findById(id).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity.ok(invoice.toDto())
    }

    override fun getPurchaseInvoicePdf(id: Long): ResponseEntity<Resource> {
        val invoice = purchaseInvoiceRepository.findById(id).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.contentDisposition = ContentDisposition.attachment()
            .filename("purchase-invoice-$id.pdf")
            .build()
        return ResponseEntity(ByteArrayResource(invoice.pdfData), headers, HttpStatus.OK)
    }

    private fun PurchaseInvoice.toDto(): PurchaseInvoiceResponseDto =
        PurchaseInvoiceResponseDto(
            id = id!!,
            productName = productName,
            amount = amount,
            price = price.toDouble(),
            invoiceDate = invoiceDate,
            createdAt = OffsetDateTime.of(createdAt, ZoneOffset.UTC),
            updatedAt = OffsetDateTime.of(updatedAt, ZoneOffset.UTC)
        )
}
