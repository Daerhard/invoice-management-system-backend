package invoice.management.system.mailSystem.services

import invoice.management.system.entities.Invoice
import invoice.management.system.mailSystem.entities.EmailAttachment
import invoice.management.system.model.InvoiceEmailRequestDto
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class InvoiceEmailPreparationService {

    fun prepare(invoice: Invoice, requestDto: InvoiceEmailRequestDto?): PreparedInvoiceEmail? {
        val recipient = requestDto?.to
            ?: invoice.order.customer.email
            ?: return null

        val pdfBytes = invoice.invoicePdf ?: return null

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val order = invoice.order
        val fileName =
            "${order.dateOfPayment.format(formatter)} Rechnung ${order.externalOrderId} - ${order.customer.fullName}.pdf"

        val subject = requestDto?.subject
            ?: "Ihre Rechnung - Bestellnummer ${order.externalOrderId}"
        val body =
            "Sehr geehrter Kunde,\n\nim Anhang finden Sie Ihre Rechnung, Bestellnummer ${order.externalOrderId}.\n\nFreundliche Gruesse"

        return PreparedInvoiceEmail(
            recipient = recipient,
            emailRequest = EmailRequest(
                to = recipient,
                subject = subject,
                body = body,
                attachments = listOf(EmailAttachment(fileName = fileName, content = pdfBytes)),
            ),
        )
    }
}
