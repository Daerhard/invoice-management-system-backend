package invoice.management.system.services.invoiceGeneration.pdfGeneration

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.events.Event
import com.itextpdf.kernel.events.IEventHandler
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue

class FooterEventHandler : IEventHandler {

    override fun handleEvent(event: Event) {
        val docEvent = event as PdfDocumentEvent
        val pdfDoc = docEvent.document
        val pageSize: PageSize = pdfDoc.defaultPageSize

        val pdfCanvas = PdfCanvas(docEvent.page)

        val footerHeight = 80F
        val footerRectangle = Rectangle(
            pageSize.left + 36f,
            pageSize.bottom,
            pageSize.width - 72f,
            footerHeight
        )

        val canvas = Canvas(pdfCanvas, footerRectangle)

        val footerTable = createInvoiceFooterTable()
        canvas.add(footerTable)
        canvas.close()
    }

    private fun createInvoiceFooterTable(): Table {
        val footerTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()

        footerTable
            .addCell(createCell("Thomas-Morus-Str. 2"))
            .addCell(createCell("Kontaktinformation", textAlignment = TextAlignment.RIGHT))
            .addCell(createCell("86916 Kaufering"))
            .addCell(createCell("Daniel Erhard", textAlignment = TextAlignment.RIGHT))
            .addCell(createCell("Deutschland"))
            .addCell(createCell("Tel: 016097506045", textAlignment = TextAlignment.RIGHT))
            .addCell(createCell("Ustd.ID-Nr.: DE360327004"))
            .addCell(createCell("E-Mail: erhard-daniel-gew@gmx.de", textAlignment = TextAlignment.RIGHT))

        return footerTable
    }

    private fun createCell(
        content: String,
        textAlignment: TextAlignment = TextAlignment.LEFT,
    ): Cell {
        return Cell().add(Paragraph(content)
            .setFontSize((8F)))
            .setBackgroundColor(ColorConstants.WHITE)
            .setTextAlignment(textAlignment)
            .setBorder(Border.NO_BORDER)
    }
}
