package invoice.management.system.services.invoiceCreation

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.Date

@Service
class InvoicePdfService {

    fun generateInvoicePdf(): ByteArray {
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter(baos)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)

        // (Sender)
        document.add(Paragraph().add(Text("Thomas-Morus-Str. 2\n")))
        document.add(Paragraph().add(Text("86916 Kaufering\n")))
        document.add(Paragraph().add(Text("Deutschland\n")))
        document.add(Paragraph().add(Text("Tel: 016097506045\n")))
        document.add(Paragraph().add(Text("E-Mail: erhard-daniel-gew@gmx.de\n")))
        document.add(Paragraph().add(Text("Ustd.ID-Nr.: DE360327004\n")))

        document.add(Paragraph())

        // Add invoice date and order info
        document.add(Paragraph().add(Text("Rechnungsdatum: ${Date()}")))
        document.add(Paragraph().add(Text("Leistungsdatum: ${Date()}")))
        document.add(Paragraph().add(Text("Bestellnummer: 1099030352")))
        document.add(Paragraph().add(Text("Rechnungsnummer: 1099030352")))

        document.add(Paragraph())

        // Add customer info (Receiver)
        document.add(Paragraph().add(Text("Levent Adem")))
        document.add(Paragraph().add(Text("Bruchstraße 64a")))
        document.add(Paragraph().add(Text("40235 Düsseldorf")))

        document.add(Paragraph())

        // Add greeting and order details
        document.add(Paragraph().add(Text("Sehr geehrter Kunde,\n\n")))
        document.add(Paragraph().add(Text("vielen Dank für Ihre Bestellung!")))
        document.add(Paragraph().add(Text("Diese beinhaltet die nachfolgend aufgeführten Positionen:\n\n")))

        // Order items
        document.add(Paragraph().add(Text("Artikel")))
        document.add(Paragraph().add(Text("3x Thunder Dragonhawk (2019 Gold Sarcophagus Tin Mega Pack) 168 Secret Rare - 1,70 €")))
        document.add(Paragraph().add(Text("Versandkosten 1,15 €")))
        document.add(Paragraph().add(Text("Gesamtbetrag 6,25 €")))

        document.add(Paragraph())

        // Add payment terms
        document.add(Paragraph().add(Text("Der ausgewiesene Betrag ist unmittelbar und ohne jeglichen Abzug zu entrichten.")))
        document.add(Paragraph().add(Text("Zu beachten: Gemäß § 19 UStG wird keine Umsatzsteuer berechnet.")))

        document.add(Paragraph())

        // Closing remark
        document.add(Paragraph().add(Text("Freundliche Grüße\n")))
        document.add(Paragraph().add(Text("Daniel Erhard")))

        // Close the document
        document.close()

        return baos.toByteArray()
    }
}
