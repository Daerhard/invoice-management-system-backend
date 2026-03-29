package invoice.management.system.services.email

import invoice.management.system.entities.Invoice
import invoice.management.system.model.InvoiceEmailRequestDto
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class InvoiceEmailPreparationService {

    fun prepare(
        invoice: Invoice,
        request: InvoiceEmailRequestDto?,
    ): PreparedInvoiceEmail? {
        val recipient = request?.to ?: invoice.order.customer.email ?: return null
        val pdfBytes = invoice.invoicePdf ?: return null

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val order = invoice.order
        val fileName =
            "${order.dateOfPayment.format(formatter)} Rechnung ${order.externalOrderId} - ${order.customer.fullName}.pdf"

        val subject = request?.subject ?: "Ihre Rechnung - Bestellnummer ${order.externalOrderId}"

        val emailRequest = EmailRequest(
            to = recipient,
            subject = subject,
            body = "Sehr geehrter Kunde,\n\nim Anhang finden Sie Ihre Rechnung, Bestellnummer ${order.externalOrderId}.\n\nFreundliche Gruesse",
            attachments = listOf(EmailAttachment(fileName = fileName, content = pdfBytes)),
        )

        return PreparedInvoiceEmail(recipient = recipient, emailRequest = emailRequest)
    }
}

data class PreparedInvoiceEmail(
    val recipient: String,
    val emailRequest: EmailRequest,
)

