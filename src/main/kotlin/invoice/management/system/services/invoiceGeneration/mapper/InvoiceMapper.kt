package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.Invoice
import invoice.management.system.model.InvoiceDto
import java.time.ZoneOffset

fun Invoice.toDto(): InvoiceDto = InvoiceDto(
    id = this.id,
    orderId = this.order.externalOrderId,
    createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
    invoicePdf = this.invoicePdf,
)
