package invoice.management.system.serviceTests

import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createInvoice
import invoice.management.system.zugferd.services.ZugferdInvoiceResult
import invoice.management.system.zugferd.services.ZugferdManager
import invoice.management.system.zugferd.services.ZugferdService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus

class ZugferdServiceTest {

    private val zugferdManager: ZugferdManager = mock(ZugferdManager::class.java)
    private val zugferdService = ZugferdService(zugferdManager)

    // ========================== createZugferdInvoice ==========================

    @Test
    fun whenOrderNotFound_thenReturn404() {
        `when`(zugferdManager.createAndSaveZugferdInvoice(99L))
            .thenReturn(ZugferdInvoiceResult.OrderNotFound("Order with id 99 not found."))

        val response = zugferdService.createZugferdInvoice(99L)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun whenInvoiceAlreadyExists_thenReturn409() {
        `when`(zugferdManager.createAndSaveZugferdInvoice(42L))
            .thenReturn(ZugferdInvoiceResult.AlreadyExists("Invoice already exists for order 42."))

        val response = zugferdService.createZugferdInvoice(42L)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @Test
    fun whenSuccessfullyCreated_thenReturn201WithInvoice() {
        val order = createCardmarketOrder(orderId = 42L)
        val invoice = createInvoice(order = order, invoicePdf = "ZUGFERD".toByteArray())
        `when`(zugferdManager.createAndSaveZugferdInvoice(42L))
            .thenReturn(ZugferdInvoiceResult.Success(invoice))

        val response = zugferdService.createZugferdInvoice(42L)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(42L, response.body?.orderId)
    }

    // ========================== getZugferdInvoiceById ==========================

    @Test
    fun whenInvoiceNotFoundById_thenReturn404() {
        `when`(zugferdManager.findById(99L)).thenReturn(null)

        val response = zugferdService.getZugferdInvoiceById(99L)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun whenInvoiceFoundById_thenReturn200WithInvoice() {
        val order = createCardmarketOrder(orderId = 42L)
        val invoice = createInvoice(order = order, invoicePdf = "ZUGFERD".toByteArray())
        `when`(zugferdManager.findById(1L)).thenReturn(invoice)

        val response = zugferdService.getZugferdInvoiceById(1L)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(42L, response.body?.orderId)
    }
}
