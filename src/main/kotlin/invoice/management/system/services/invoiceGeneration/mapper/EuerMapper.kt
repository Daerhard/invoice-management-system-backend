package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.EuerPosition
import invoice.management.system.entities.EuerReport
import invoice.management.system.model.EuerPositionDto
import invoice.management.system.model.EuerReportDto
import invoice.management.system.model.EuerSectionDto

fun EuerReport.toDto(): EuerReportDto = EuerReportDto(
    id = this.id,
    year = this.year,
    published = this.published,
    createdAt = this.createdAt,
    positions = this.positions.map { it.toDto() }
)

fun EuerPosition.toDto(): EuerPositionDto = EuerPositionDto(
    id = this.id,
    euerReportId = this.euerReport?.id,
    section = EuerSectionDto.valueOf(this.section.name),
    description = this.description,
    value = this.value.toDouble(),
    automaticallyCalculated = this.automaticallyCalculated
)
