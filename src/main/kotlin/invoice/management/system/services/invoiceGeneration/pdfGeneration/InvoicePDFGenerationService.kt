package invoice.management.system.services.invoiceGeneration.pdfGeneration

import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import invoice.management.system.entities.CardmarketOrder
import invoice.management.system.entities.OrderItem
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class InvoicePDFGenerationService {

    private val smallFontSize = 8f

    fun generateInvoicePdf(cardmarketOrder: CardmarketOrder): ByteArray {
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter(baos)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)
        document.setMargins(36F, 36F, 120F, 36F)
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, FooterEventHandler())

        createInvoiceHeader(document, cardmarketOrder)
        document.add(Paragraph("\n"))

        createCustomerDetails(document, cardmarketOrder)
        document.add(Paragraph("\n"))

        document.add(createParagraph("Sehr geehrter Kunde,", smallFontSize))
        document.add(createParagraph("vielen Dank für Ihre Bestellung!", smallFontSize))
        document.add(createParagraph("Diese beinhaltet die nachfolgend aufgeführten Positionen:", smallFontSize))
        document.add(Paragraph("\n"))

        createOrderTable(document, cardmarketOrder)
        document.add(Paragraph("\n"))

        createFinalPart(document)
        document.close()

        return baos.toByteArray()
    }

    private fun createInvoiceHeader(document: Document, cardmarketOrder: CardmarketOrder) {
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(50f))).useAllAvailableWidth()

        val invoiceDate = cardmarketOrder.dateOfPayment
        val paymentDate = cardmarketOrder.dateOfPayment
        val orderNumber = cardmarketOrder.externalOrderId
        val invoiceNumber = cardmarketOrder.externalOrderId

        headerTable
            .addCell(createCell("Rechnungsdatum: $invoiceDate", textAlignment = TextAlignment.RIGHT))
            .addCell(createCell("Leistungsdatum: $paymentDate",textAlignment = TextAlignment.RIGHT))
            .addCell(createCell("Bestellnummer: $orderNumber", textAlignment = TextAlignment.RIGHT))
            .addCell(createCell("Rechnungsnummer: $invoiceNumber", textAlignment = TextAlignment.RIGHT))

        document.add(headerTable)
    }

    private fun createCustomerDetails(document: Document, cardmarketOrder: CardmarketOrder){
        val fullName = cardmarketOrder.customer.fullName
        val street = cardmarketOrder.customer.street
        val city = cardmarketOrder.customer.city

        document.add(createParagraph(fullName, smallFontSize))
        document.add(createParagraph(street, smallFontSize))
        document.add(createParagraph(city, smallFontSize))
    }

    private fun createOrderTable(document: Document, cardmarketOrder: CardmarketOrder) {
        val shipmentCost = cardmarketOrder.shipmentCost
        val totalValue = cardmarketOrder.totalValue

        val orderTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 60f, 15f, 15f))).useAllAvailableWidth()
        orderTable.addHeaderCell(createCell(" Anzahl",bold = true, backgroundColor = ColorConstants.LIGHT_GRAY).setBorderRight(SolidBorder(0.5f)).setBorderBottom(SolidBorder(1f)))
        orderTable.addHeaderCell(createCell(" Artikelbeschreibung", bold = true, backgroundColor = ColorConstants.LIGHT_GRAY).setBorderRight(SolidBorder(0.5f)).setBorderBottom(SolidBorder(1f)))
        orderTable.addHeaderCell(createCell(" Einzelpreis", bold = true, backgroundColor = ColorConstants.LIGHT_GRAY).setBorderRight(SolidBorder(0.5f)).setBorderBottom(SolidBorder(1f)))
        orderTable.addHeaderCell(createCell(" Gesamtpreis", bold = true, backgroundColor = ColorConstants.LIGHT_GRAY).setBorderBottom(SolidBorder(1f)))

        cardmarketOrder.orderItems.map { orderItem ->
                createArticleRow(orderTable, orderItem)
        }

        createEmptyBorderRow(orderTable, true)

        orderTable.addCell(createCell("Versandkosten").setBorderRight(SolidBorder(0.5f)))
        orderTable.addCell(createCell(""))
        orderTable.addCell(createCell("").setBorderRight(SolidBorder(0.5f)))
        orderTable.addCell(createCell(String.format("%.2f", shipmentCost) + " €"))

        createEmptyBorderRow(orderTable, false)

        orderTable.addCell(createCell("Gesamtbetrag").setBorderRight(SolidBorder(0.5f)))
        orderTable.addCell(createCell(""))
        orderTable.addCell(createCell("").setBorderRight(SolidBorder(0.5f)))
        orderTable.addCell(createCell(String.format("%.2f", totalValue) + " €"))

        document.add(orderTable)
    }

    private fun createArticleRow(orderTable: Table, orderItem: OrderItem) {
        val count = orderItem.count.toString()
        val card = orderItem.card
        val isfirstEdition = if (orderItem.isFirstEdition) " - 1. Edition" else ""
        val description = "${card.productName} - (${card.cardId.konamiSet}) - ${card.cardId.number} - ${orderItem.card.rarity} - ${orderItem.condition}${isfirstEdition}"
        val singlePrice = String.format("%.2f", orderItem.price)
        val totalPrice =  String.format("%.2f", (orderItem.price * orderItem.count))

        orderTable.addCell(createCell("$count x").setBorderRight(SolidBorder(0.5f)))
        orderTable.addCell(createCell(description).setBorderRight(SolidBorder(0.5f)).setMaxWidth(40f))
        orderTable.addCell(createCell("$singlePrice €").setBorderRight(SolidBorder(0.5f)))
        orderTable.addCell(createCell("$totalPrice €"))
    }

    private fun createEmptyBorderRow(orderTable: Table, isArticleRowEnd: Boolean){
        orderTable.addCell(createCell("").setBorderRight(SolidBorder(0.5f)).setBorderBottom(SolidBorder(0.5f)))

        if (isArticleRowEnd) {
            orderTable.addCell(createCell("").setBorderBottom(SolidBorder(0.5f)).setBorderRight(SolidBorder(0.5f)))
        } else {
            orderTable.addCell(createCell("").setBorderBottom(SolidBorder(0.5f)))
        }

        orderTable.addCell(createCell("").setBorderRight(SolidBorder(0.5f)).setBorderBottom(SolidBorder(0.5f)))
        orderTable.addCell(createCell("").setBorderBottom(SolidBorder(0.5f)))
    }

    private fun createFinalPart(document: Document) {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(50f))).useAllAvailableWidth()

        table
            .addCell(createCell("Der ausgewiesene Betrag ist unmittelbar und ohne jeglichen Abzug zu entrichten."))
            .addCell(createCell("Zu beachten: Gemäß § 19 UStG wird keine Umsatzsteuer berechnet."))
            .addCell(createCell(""))
            .addCell(createCell(""))
            .addCell(createCell(""))
            .addCell(createCell(""))
            .addCell(createCell("Freundliche Grüße"))
            .addCell(createCell("Daniel Erhard"))
            .addCell(createCell(""))
            .addCell(createCell(""))
            .addCell(createCell(""))

        document.add(table)
    }

    private fun createCell(
        content: String,
        textAlignment: TextAlignment = TextAlignment.LEFT,
        fontSize: Float = smallFontSize,
        border: Border = Border.NO_BORDER,
        backgroundColor: Color = ColorConstants.WHITE,
        bold: Boolean = false
    ): Cell {
        val cell = Cell().add(Paragraph(content)
            .setFontSize(fontSize))
            .setBackgroundColor(backgroundColor)
            .setTextAlignment(textAlignment)
            .setBorder(border)

        return if (bold) cell.setBold() else cell
    }

    private fun createParagraph(content: String, fontSize: Float, bold: Boolean = false): Paragraph {
        val paragraph = Paragraph(content).setFontSize(fontSize)
        if (bold) {
            paragraph.setBold()
        }
        return paragraph
    }
}
