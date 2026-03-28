package invoice.management.system.services.invoiceGeneration.mapper

import invoice.management.system.entities.User
import invoice.management.system.model.UserDto

fun User.toDto(): UserDto = UserDto(
    id = this.id,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    zipCode = this.zipCode,
    city = this.city,
    street = this.street,
    email = this.email,
)

fun UserDto.toEntity(): User = User(
    username = this.username,
    password = requireNotNull(this.password) { "password must not be null" },
    firstName = this.firstName,
    lastName = this.lastName,
    zipCode = this.zipCode,
    city = this.city,
    street = this.street,
    email = this.email,
)
