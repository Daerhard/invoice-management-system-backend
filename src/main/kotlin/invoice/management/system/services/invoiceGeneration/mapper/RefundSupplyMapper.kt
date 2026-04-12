package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.Refund
import invoice.management.system.entities.Supply
import invoice.management.system.entities.SupplyInvoiceDocument
import invoice.management.system.model.RefundDto
import invoice.management.system.model.SupplyDto
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource

fun Refund.toDto(): RefundDto = RefundDto(
    id = this.id,
    value = this.value.toDouble(),
    year = this.year
)

fun RefundDto.toEntity(): Refund = Refund(
    value = this.value.toBigDecimal(),
    year = this.year
)

fun Supply.toDto(): SupplyDto = SupplyDto(
    id = this.id,
    value = this.value.toDouble(),
    supplyDate = this.supplyDate,
    product = this.product
)

fun SupplyDto.toEntity(): Supply = Supply(
    value = this.value.toBigDecimal(),
    supplyDate = this.supplyDate,
    product = this.product
)

fun SupplyInvoiceDocument.toResource(): Resource = ByteArrayResource(this.pdfData)
