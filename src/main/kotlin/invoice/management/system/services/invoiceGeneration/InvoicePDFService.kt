package invoice.management.system.services.invoiceGeneration

import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.layout.Document
import invoice.management.system.api.InvoiceGenerationPDFApiDelegate
import invoice.management.system.entities.CardmarketOrder
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.services.csvImport.anniversaryEditionRegex
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class InvoicePDFService(
    private val invoicePDFGenerationService: InvoicePDFGenerationService,
    private val cardmarketOrderRepository: CardmarketOrderRepository
) : InvoiceGenerationPDFApiDelegate {

    override fun getPDFInvoice(cardmarketExternalOrderId: Long): ResponseEntity<Resource> {
        val cardmarketOrder = cardmarketOrderRepository.findByExternalOrderId(cardmarketExternalOrderId)
            ?: return ResponseEntity.notFound().build()

        val invoicePDF = invoicePDFGenerationService.generateInvoicePdf(cardmarketOrder)

        val resource = ByteArrayResource(invoicePDF)

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val fileName = "${cardmarketOrder.dateOfPayment.format(formatter)} Rechnung ${cardmarketOrder.externalOrderId} - ${cardmarketOrder.customer.fullName}.pdf"

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; $fileName")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(invoicePDF.size.toLong())
            .body(resource)
    }

    override fun getPDFInvoices(startDate: LocalDate, endDate: LocalDate): ResponseEntity<ByteArray> {
        val cardmarketOrders = cardmarketOrderRepository.findByStartDateAndEndDate(startDate, endDate)

        val invoices = cardmarketOrders.map {
            invoiceData(
                it,
                invoicePDFGenerationService.generateInvoicePdf(it)
            )
        }

        val zipFile = createZipWithInvoices(invoices)

        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoices_${startDate}_to_${endDate}.zip")
        headers.add(HttpHeaders.CONTENT_TYPE, "application/zip")

        return ResponseEntity(zipFile, headers, HttpStatus.OK)
    }

    private fun createZipWithInvoices(invoices: List<invoiceData>): ByteArray {
        val baos = ByteArrayOutputStream()
        val zipOut = ZipOutputStream(baos)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        invoices.forEachIndexed { index, invoice ->
            val cardmarketOrder = invoice.cardmarketOrder
            val fileName = "${cardmarketOrder.dateOfPayment.format(formatter)} Rechnung ${cardmarketOrder.externalOrderId} - ${cardmarketOrder.customer.fullName}.pdf"
            val zipEntry = ZipEntry(fileName)
            zipOut.putNextEntry(zipEntry)

            zipOut.write(invoice.invoiceDocument)
            zipOut.closeEntry()
        }

        zipOut.close()
        return baos.toByteArray()
    }

    data class invoiceData(
        val cardmarketOrder: CardmarketOrder,
        val invoiceDocument: ByteArray
    )
}
