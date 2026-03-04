package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.PurchaseInvoice
import invoice.management.system.entities.PurchaseInvoiceDocument
import invoice.management.system.model.PurchaseInvoiceDto
import org.springframework.core.io.Resource


fun PurchaseInvoice.toDto() : PurchaseInvoiceDto = PurchaseInvoiceDto(
    id = this.id,
    productName = this.productName,
    amount = this.amount,
    price = this.price.toDouble(),
    invoiceDate = this.invoiceDate,
)

fun PurchaseInvoiceDto.toEntity(purchaseInvoiceDocument: PurchaseInvoiceDocument): PurchaseInvoice = PurchaseInvoice(
    productName = this.productName,
    amount = this.amount,
    price = this.price.toBigDecimal(),
    invoiceDate = this.invoiceDate,
    document = purchaseInvoiceDocument
).apply { this.id = this.id }

fun PurchaseInvoiceDocument.toResource() : Resource {
    return org.springframework.core.io.ByteArrayResource(this.pdfData)
}