package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.PurchaseInvoice
import invoice.management.system.entities.PurchaseInvoiceDocument
import invoice.management.system.entities.PurchaseInvoiceItem
import invoice.management.system.entities.PurchaseType
import invoice.management.system.model.PurchaseInvoiceDto
import invoice.management.system.model.PurchaseInvoiceItemDto
import org.springframework.core.io.Resource


fun PurchaseInvoice.toDto(): PurchaseInvoiceDto = PurchaseInvoiceDto(
    id = this.id,
    productName = this.productName,
    totalPrice = this.totalPrice.toDouble(),
    items = this.items.map { it.toDto() }
)

fun PurchaseInvoiceDto.toEntity(): PurchaseInvoice = PurchaseInvoice(
    productName = this.productName,
)

fun PurchaseInvoiceItem.toDto(): PurchaseInvoiceItemDto = PurchaseInvoiceItemDto(
    id = this.id,
    purchaseType = PurchaseInvoiceItemDto.PurchaseType.valueOf(this.purchaseType.name),
    amount = this.amount,
    price = this.price.toDouble(),
    invoiceDate = this.invoiceDate,
)

fun PurchaseInvoiceItemDto.toEntity(): PurchaseInvoiceItem = PurchaseInvoiceItem(
    purchaseType = PurchaseType.valueOf(this.purchaseType.name),
    amount = this.amount,
    price = this.price.toBigDecimal(),
    invoiceDate = this.invoiceDate,
)

fun PurchaseInvoiceDocument.toResource(): Resource {
    return org.springframework.core.io.ByteArrayResource(this.pdfData)
}
