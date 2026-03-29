package invoice.management.system.serviceTests

import invoice.management.system.entities.Invoice
import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createInvoice
import invoice.management.system.repositories.CardmarketOrderRepository
import invoice.management.system.repositories.InvoiceRepository
import invoice.management.system.zugferd.services.ZugferdInvoiceResult
import invoice.management.system.zugferd.services.ZugferdManager
import invoice.management.system.zugferd.services.ZugferdGenerationService
import invoice.management.system.services.invoiceGeneration.pdfGeneration.InvoicePDFGenerationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@Suppress("UNCHECKED_CAST")
private fun <T> anyNonNull(): T = Mockito.any<T>() as T

class ZugferdManagerTest {

    private val cardmarketOrderRepository: CardmarketOrderRepository = mock(CardmarketOrderRepository::class.java)
    private val invoiceRepository: InvoiceRepository = mock(InvoiceRepository::class.java)
    private val invoicePDFGenerationService: InvoicePDFGenerationService = mock(InvoicePDFGenerationService::class.java)
    private val zugferdGenerationService: ZugferdGenerationService = mock(ZugferdGenerationService::class.java)

    private val zugferdManager = ZugferdManager(
        cardmarketOrderRepository,
        invoiceRepository,
        invoicePDFGenerationService,
        zugferdGenerationService,
    )

    // ========================== createAndSaveZugferdInvoice ==========================

    @Test
    fun whenOrderNotFound_thenReturnOrderNotFound() {
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(null)

        val result = zugferdManager.createAndSaveZugferdInvoice(42L)

        assertEquals(
            ZugferdInvoiceResult.OrderNotFound("Order with id 42 not found."),
            result,
        )
    }

    @Test
    fun whenInvoiceAlreadyExists_thenReturnAlreadyExists() {
        val order = createCardmarketOrder(orderId = 42L)
        val existingInvoice = createInvoice(order = order)
        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoiceRepository.findByOrder(order)).thenReturn(existingInvoice)

        val result = zugferdManager.createAndSaveZugferdInvoice(42L)

        assertEquals(
            ZugferdInvoiceResult.AlreadyExists("Invoice already exists for order 42."),
            result,
        )
    }

    @Test
    fun whenOrderFoundAndNoExistingInvoice_thenGenerateAndSave() {
        val order = createCardmarketOrder(orderId = 42L)
        val pdfBytes = "PDF".toByteArray()
        val zugferdBytes = "ZUGFERD_PDF".toByteArray()

        `when`(cardmarketOrderRepository.findByExternalOrderId(42L)).thenReturn(order)
        `when`(invoiceRepository.findByOrder(order)).thenReturn(null)
        `when`(invoicePDFGenerationService.generateInvoicePdf(order)).thenReturn(pdfBytes)
        `when`(zugferdGenerationService.generateZugferdInvoice(order, pdfBytes)).thenReturn(zugferdBytes)

        val savedInvoice = createInvoice(order = order, invoicePdf = zugferdBytes)
        `when`(invoiceRepository.save(anyNonNull<Invoice>())).thenReturn(savedInvoice)

        val result = zugferdManager.createAndSaveZugferdInvoice(42L)

        assert(result is ZugferdInvoiceResult.Success)
        val success = result as ZugferdInvoiceResult.Success
        assertEquals(savedInvoice, success.invoice)

        val captor = ArgumentCaptor.forClass(Invoice::class.java)
        verify(invoiceRepository).save(captor.capture())
        assertNotNull(captor.value.invoicePdf)
    }
}
