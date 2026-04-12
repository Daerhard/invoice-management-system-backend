package invoice.management.system.serviceTests

import invoice.management.system.api.NotFoundException
import invoice.management.system.entities.PurchaseInvoice
import invoice.management.system.entities.PurchaseInvoiceDocument
import invoice.management.system.factories.EntityFactory.Companion.createPurchaseInvoice
import invoice.management.system.factories.EntityFactory.Companion.createPurchaseInvoiceItem
import invoice.management.system.model.PurchaseInvoiceDto
import invoice.management.system.model.PurchaseInvoiceItemDto
import invoice.management.system.repositories.PurchaseInvoiceItemRepository
import invoice.management.system.repositories.PurchaseInvoiceRepository
import invoice.management.system.services.purchaseInvoice.PurchaseInvoiceService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class PurchaseInvoiceServiceTest {

    private val purchaseInvoiceRepository: PurchaseInvoiceRepository = mock(PurchaseInvoiceRepository::class.java)
    private val purchaseInvoiceItemRepository: PurchaseInvoiceItemRepository = mock(PurchaseInvoiceItemRepository::class.java)
    private val service = PurchaseInvoiceService(purchaseInvoiceRepository, purchaseInvoiceItemRepository)

    private val validPdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x01)
    private val invalidPdfBytes = byteArrayOf(0x00, 0x01, 0x02, 0x03)

    @Test
    fun whenCreatePurchaseInvoice_thenReturnCreatedDto() {
        val invoice = createPurchaseInvoice(productName = "Booster Box")
        val dto = PurchaseInvoiceDto(productName = "Booster Box")
        `when`(purchaseInvoiceRepository.save(any(PurchaseInvoice::class.java))).thenReturn(invoice)

        val response = service.createPurchaseInvoice(dto)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Booster Box", response.body?.productName)
        assertEquals(0.0, response.body?.totalPrice)
        assertNotNull(response.body?.items)
        assertEquals(0, response.body?.items?.size)
    }

    @Test
    fun whenAddPurchaseInvoiceItem_withValidPdf_thenReturnUpdatedInvoice() {
        val invoice = createPurchaseInvoice(productName = "Booster Box")
        val itemDto = PurchaseInvoiceItemDto(
            purchaseType = PurchaseInvoiceItemDto.PurchaseType.BOOSTER,
            amount = 2,
            price = 9.99,
            invoiceDate = LocalDate.of(2024, 1, 15)
        )
        val pdfResource = ByteArrayResource(validPdfBytes)

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceRepository.save(any(PurchaseInvoice::class.java))).thenAnswer { it.arguments[0] as PurchaseInvoice }

        val response = service.addPurchaseInvoiceItem(1, itemDto, pdfResource)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Booster Box", response.body?.productName)
        assertEquals(1, response.body?.items?.size)
        assertEquals(PurchaseInvoiceItemDto.PurchaseType.BOOSTER, response.body?.items?.first()?.purchaseType)
        assertEquals(2, response.body?.items?.first()?.amount)
        assertNotNull(response.body?.totalPrice)
    }

    @Test
    fun whenAddPurchaseInvoiceItem_withoutPdf_thenItemAddedWithoutDocument() {
        val invoice = createPurchaseInvoice(productName = "Display Box")
        val itemDto = PurchaseInvoiceItemDto(
            purchaseType = PurchaseInvoiceItemDto.PurchaseType.DISPLAY,
            amount = 1,
            price = 89.99,
            invoiceDate = LocalDate.of(2024, 3, 1)
        )

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceRepository.save(any(PurchaseInvoice::class.java))).thenAnswer { it.arguments[0] as PurchaseInvoice }

        val response = service.addPurchaseInvoiceItem(1, itemDto, null)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(1, response.body?.items?.size)
        assertNull(invoice.items.first().document)
    }

    @Test
    fun whenAddPurchaseInvoiceItem_withInvalidPdf_thenReturnBadRequest() {
        val invoice = createPurchaseInvoice()
        val itemDto = PurchaseInvoiceItemDto(
            purchaseType = PurchaseInvoiceItemDto.PurchaseType.CASE,
            amount = 3,
            price = 19.99,
            invoiceDate = LocalDate.of(2024, 6, 1)
        )
        val invalidPdfResource = ByteArrayResource(invalidPdfBytes)

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))

        val response = service.addPurchaseInvoiceItem(1, itemDto, invalidPdfResource)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun whenAddPurchaseInvoiceItem_withNonExistingInvoice_thenThrowNotFoundException() {
        `when`(purchaseInvoiceRepository.findById(999)).thenReturn(Optional.empty())
        val itemDto = PurchaseInvoiceItemDto(
            purchaseType = PurchaseInvoiceItemDto.PurchaseType.BOOSTER,
            amount = 1,
            price = 5.0,
            invoiceDate = LocalDate.now()
        )

        assertThrows(NotFoundException::class.java) {
            service.addPurchaseInvoiceItem(999, itemDto, null)
        }
    }

    @Test
    fun whenAddMultipleItems_thenTotalPriceIsCalculatedCorrectly() {
        val invoice = createPurchaseInvoice(productName = "Mixed Order")
        val item1Dto = PurchaseInvoiceItemDto(
            purchaseType = PurchaseInvoiceItemDto.PurchaseType.BOOSTER,
            amount = 2,
            price = 10.00,
            invoiceDate = LocalDate.of(2024, 1, 1)
        )
        val item2Dto = PurchaseInvoiceItemDto(
            purchaseType = PurchaseInvoiceItemDto.PurchaseType.DISPLAY,
            amount = 1,
            price = 30.00,
            invoiceDate = LocalDate.of(2024, 1, 2)
        )

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceRepository.save(any(PurchaseInvoice::class.java))).thenAnswer { it.arguments[0] as PurchaseInvoice }

        service.addPurchaseInvoiceItem(1, item1Dto, null)
        val response = service.addPurchaseInvoiceItem(1, item2Dto, null)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(2, response.body?.items?.size)
        // 2 * 10.00 + 1 * 30.00 = 50.00
        assertEquals(50.00, response.body?.totalPrice)
    }

    @Test
    fun whenGetAllPurchaseInvoices_thenReturnAllInvoices() {
        val invoice1 = createPurchaseInvoice(productName = "Invoice 1")
        val invoice2 = createPurchaseInvoice(productName = "Invoice 2")
        `when`(purchaseInvoiceRepository.findAll()).thenReturn(listOf(invoice1, invoice2))

        val response = service.getAllPurchaseInvoices()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
        assertEquals("Invoice 1", response.body?.get(0)?.productName)
        assertEquals("Invoice 2", response.body?.get(1)?.productName)
    }

    @Test
    fun whenGetPurchaseInvoiceItemPdf_withDocument_thenReturnPdf() {
        val invoice = createPurchaseInvoice()
        val item = createPurchaseInvoiceItem(invoice = invoice)
        val pdfData = validPdfBytes
        val document = PurchaseInvoiceDocument(pdfData)
        item.attachDocument(document)

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceItemRepository.findById(10)).thenReturn(Optional.of(item))

        val response = service.getPurchaseInvoiceItemPdf(1, 10)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun whenGetPurchaseInvoiceItemPdf_withMissingInvoice_thenThrowNotFoundException() {
        `when`(purchaseInvoiceRepository.findById(999)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.getPurchaseInvoiceItemPdf(999, 1)
        }
    }

    @Test
    fun whenGetPurchaseInvoiceItemPdf_withMissingItem_thenThrowNotFoundException() {
        val invoice = createPurchaseInvoice()
        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceItemRepository.findById(999)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.getPurchaseInvoiceItemPdf(1, 999)
        }
    }

    @Test
    fun whenDeletePurchaseInvoiceItem_withValidData_thenReturn204() {
        val invoice = createPurchaseInvoice(productName = "Booster Box")
        val item = createPurchaseInvoiceItem(invoice = invoice)
        invoice.addItem(item)

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceItemRepository.findById(10)).thenReturn(Optional.of(item))
        `when`(purchaseInvoiceRepository.save(any(PurchaseInvoice::class.java))).thenAnswer { it.arguments[0] as PurchaseInvoice }

        val response = service.deletePurchaseInvoiceItem(1, 10)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun whenDeletePurchaseInvoiceItem_thenTotalPriceRecalculated() {
        val invoice = createPurchaseInvoice(productName = "Booster Box")
        val item1 = createPurchaseInvoiceItem(amount = 2, price = BigDecimal("10.00"), invoice = invoice)
        val item2 = createPurchaseInvoiceItem(amount = 1, price = BigDecimal("30.00"), invoice = invoice)
        invoice.addItem(item1)
        invoice.addItem(item2)

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceItemRepository.findById(10)).thenReturn(Optional.of(item1))
        `when`(purchaseInvoiceRepository.save(any(PurchaseInvoice::class.java))).thenAnswer { it.arguments[0] as PurchaseInvoice }

        service.deletePurchaseInvoiceItem(1, 10)

        // After removing item1 (2 * 10.00 = 20.00), only item2 (1 * 30.00 = 30.00) remains
        assertEquals(BigDecimal("30.00"), invoice.totalPrice)
    }

    @Test
    fun whenDeletePurchaseInvoiceItem_withMissingInvoice_thenThrowNotFoundException() {
        `when`(purchaseInvoiceRepository.findById(999)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.deletePurchaseInvoiceItem(999, 10)
        }
    }

    @Test
    fun whenDeletePurchaseInvoiceItem_withMissingItem_thenThrowNotFoundException() {
        val invoice = createPurchaseInvoice()
        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceItemRepository.findById(999)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.deletePurchaseInvoiceItem(1, 999)
        }
    }

    @Test
    fun whenDeletePurchaseInvoiceItem_withItemNotBelongingToInvoice_thenReturn409() {
        val invoice = createPurchaseInvoice()
        val otherInvoice = createPurchaseInvoice(productName = "Other Invoice")
        val item = createPurchaseInvoiceItem(invoice = otherInvoice)

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceItemRepository.findById(10)).thenReturn(Optional.of(item))

        val response = service.deletePurchaseInvoiceItem(1, 10)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }
        val invoice = createPurchaseInvoice()
        val item = createPurchaseInvoiceItem(invoice = invoice)

        `when`(purchaseInvoiceRepository.findById(1)).thenReturn(Optional.of(invoice))
        `when`(purchaseInvoiceItemRepository.findById(10)).thenReturn(Optional.of(item))

        assertThrows(NotFoundException::class.java) {
            service.getPurchaseInvoiceItemPdf(1, 10)
        }
    }
}
