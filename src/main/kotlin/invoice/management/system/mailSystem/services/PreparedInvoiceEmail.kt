package invoice.management.system.mailSystem.services

data class PreparedInvoiceEmail(
    val recipient: String,
    val emailRequest: EmailRequest,
)
