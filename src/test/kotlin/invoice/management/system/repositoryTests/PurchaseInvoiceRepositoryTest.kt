package invoice.management.system.repositoryTests

import invoice.management.system.entities.PurchaseInvoiceDocument
import invoice.management.system.entities.PurchaseType
import invoice.management.system.factories.EntityFactory.Companion.createPurchaseInvoice
import invoice.management.system.factories.EntityFactory.Companion.createPurchaseInvoiceItem
import invoice.management.system.repositories.PurchaseInvoiceItemRepository
import invoice.management.system.repositories.PurchaseInvoiceRepository
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PurchaseInvoiceRepositoryTest : RepositoryTest() {

    @Autowired
    lateinit var purchaseInvoiceRepository: PurchaseInvoiceRepository

    @Autowired
    lateinit var purchaseInvoiceItemRepository: PurchaseInvoiceItemRepository

    @Test
    fun whenSavePurchaseInvoice_thenCanBeRetrieved() {
        val invoice = createPurchaseInvoice(productName = "Booster Box Collection")
        purchaseInvoiceRepository.save(invoice)
        entityManager.flushAndClear()

        val all = purchaseInvoiceRepository.findAll()

        assertEquals(1, all.size)
        assertEquals("Booster Box Collection", all.first().productName)
        assertEquals(BigDecimal.ZERO.setScale(4), all.first().totalPrice.setScale(4))
    }

    @Test
    fun whenAddItemToInvoice_thenItemIsPersistedAndTotalPriceUpdated() {
        val invoice = createPurchaseInvoice(productName = "Mixed Box")
        val item = createPurchaseInvoiceItem(
            purchaseType = PurchaseType.BOOSTER,
            amount = 3,
            price = BigDecimal("12.50"),
            invoiceDate = LocalDate.of(2024, 5, 10)
        )
        invoice.addItem(item)
        purchaseInvoiceRepository.save(invoice)
        entityManager.flushAndClear()

        val savedInvoice = purchaseInvoiceRepository.findById(invoice.id).orElseThrow()

        assertEquals(1, savedInvoice.items.size)
        assertEquals(PurchaseType.BOOSTER, savedInvoice.items.first().purchaseType)
        assertEquals(3, savedInvoice.items.first().amount)
        assertEquals(0, BigDecimal("12.50").compareTo(savedInvoice.items.first().price))
        assertEquals(LocalDate.of(2024, 5, 10), savedInvoice.items.first().invoiceDate)
        // total = 3 * 12.50 = 37.50
        assertEquals(0, BigDecimal("37.50").compareTo(savedInvoice.totalPrice))
    }

    @Test
    fun whenAddMultipleItemsToInvoice_thenTotalPriceIsSum() {
        val invoice = createPurchaseInvoice(productName = "Big Order")
        val item1 = createPurchaseInvoiceItem(
            purchaseType = PurchaseType.DISPLAY,
            amount = 2,
            price = BigDecimal("50.00"),
            invoiceDate = LocalDate.of(2024, 1, 1)
        )
        val item2 = createPurchaseInvoiceItem(
            purchaseType = PurchaseType.CASE,
            amount = 1,
            price = BigDecimal("150.00"),
            invoiceDate = LocalDate.of(2024, 1, 2)
        )
        invoice.addItem(item1)
        invoice.addItem(item2)
        purchaseInvoiceRepository.save(invoice)
        entityManager.flushAndClear()

        val savedInvoice = purchaseInvoiceRepository.findById(invoice.id).orElseThrow()

        assertEquals(2, savedInvoice.items.size)
        // total = 2 * 50.00 + 1 * 150.00 = 250.00
        assertEquals(0, BigDecimal("250.00").compareTo(savedInvoice.totalPrice))
    }

    @Test
    fun whenAttachDocumentToItem_thenDocumentIsPersistedAndRetrievable() {
        val invoice = createPurchaseInvoice(productName = "Invoice With PDF")
        val item = createPurchaseInvoiceItem()
        val pdfData = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x01)
        invoice.addItem(item)
        item.attachDocument(PurchaseInvoiceDocument(pdfData))
        purchaseInvoiceRepository.save(invoice)
        entityManager.flushAndClear()

        val savedItem = purchaseInvoiceItemRepository.findById(item.id).orElseThrow()

        assertNotNull(savedItem.document)
        assertTrue(savedItem.document!!.pdfData.contentEquals(pdfData))
    }

    @Test
    fun whenItemHasNoDocument_thenDocumentIsNull() {
        val invoice = createPurchaseInvoice(productName = "Invoice Without PDF")
        val item = createPurchaseInvoiceItem()
        invoice.addItem(item)
        purchaseInvoiceRepository.save(invoice)
        entityManager.flushAndClear()

        val savedItem = purchaseInvoiceItemRepository.findById(item.id).orElseThrow()

        assertNull(savedItem.document)
    }

    @Test
    fun whenDeleteInvoice_thenItemsAndDocumentsAreAlsoDeleted() {
        val invoice = createPurchaseInvoice()
        val item = createPurchaseInvoiceItem()
        val pdfData = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x01)
        invoice.addItem(item)
        item.attachDocument(PurchaseInvoiceDocument(pdfData))
        purchaseInvoiceRepository.save(invoice)
        val invoiceId = invoice.id
        val itemId = item.id
        entityManager.flushAndClear()

        purchaseInvoiceRepository.deleteById(invoiceId)
        entityManager.flushAndClear()

        assertTrue(purchaseInvoiceRepository.findById(invoiceId).isEmpty)
        assertTrue(purchaseInvoiceItemRepository.findById(itemId).isEmpty)
    }

    @Test
    fun whenSaveMultipleInvoices_thenAllAreReturned() {
        purchaseInvoiceRepository.save(createPurchaseInvoice(productName = "First"))
        purchaseInvoiceRepository.save(createPurchaseInvoice(productName = "Second"))
        purchaseInvoiceRepository.save(createPurchaseInvoice(productName = "Third"))
        entityManager.flushAndClear()

        val all = purchaseInvoiceRepository.findAll()

        assertEquals(3, all.size)
    }
}
