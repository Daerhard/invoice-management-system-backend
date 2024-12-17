package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.Customer
import invoice.management.system.model.CustomerDto

fun Customer.toDto() : CustomerDto {
    return CustomerDto(
        userName = userName,
        isProfessional = isProfessional,
    )
}