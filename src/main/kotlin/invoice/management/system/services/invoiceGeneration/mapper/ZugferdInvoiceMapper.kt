package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.Invoice
import invoice.management.system.model.ZugferdInvoiceDto
import java.time.ZoneOffset

fun Invoice.toZugferdDto(): ZugferdInvoiceDto = ZugferdInvoiceDto(
    id = this.id,
    orderId = this.order.externalOrderId,
    createdAt = this.createdAt.atOffset(ZoneOffset.UTC),
    invoicePdf = this.invoicePdf,
)
