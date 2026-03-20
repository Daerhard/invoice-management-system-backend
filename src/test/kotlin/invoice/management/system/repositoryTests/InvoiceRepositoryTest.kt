package invoice.management.system.repositoryTests

import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.factories.EntityFactory.Companion.createInvoice
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Instant
import java.time.temporal.ChronoUnit

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InvoiceRepositoryTest : RepositoryTest() {

    @Test
    fun whenSaveInvoice_thenCanBeFoundByOrder() {
        val customer = entityManager.persist(createCustomer())
        val order = entityManager.persist(createCardmarketOrder(customer = customer))
        val createdAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val pdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)

        val invoice = createInvoice(order = order, createdAt = createdAt, invoicePdf = pdfBytes)
        invoiceRepository.save(invoice)
        entityManager.flushAndClear()

        val fetchedInvoice = invoiceRepository.findByOrder(order)

        assertNotNull(fetchedInvoice)
        assertEquals(createdAt, fetchedInvoice?.createdAt)
        assertArrayEquals(pdfBytes, fetchedInvoice?.invoicePdf)
        assertEquals(order.externalOrderId, fetchedInvoice?.order?.externalOrderId)
    }

    @Test
    fun whenNoInvoiceForOrder_thenFindByOrderReturnsNull() {
        val customer = entityManager.persist(createCustomer())
        val order = entityManager.persist(createCardmarketOrder(customer = customer))
        entityManager.flushAndClear()

        val fetchedInvoice = invoiceRepository.findByOrder(order)

        assertNull(fetchedInvoice)
    }

    @Test
    fun whenSaveInvoiceWithoutPdf_thenInvoicePdfIsNull() {
        val customer = entityManager.persist(createCustomer())
        val order = entityManager.persist(createCardmarketOrder(customer = customer))
        val invoice = createInvoice(order = order)
        invoiceRepository.save(invoice)
        entityManager.flushAndClear()

        val fetchedInvoice = invoiceRepository.findByOrder(order)

        assertNotNull(fetchedInvoice)
        assertNull(fetchedInvoice?.invoicePdf)
    }
}
