package invoice.management.system.zugferd.services

import invoice.management.system.api.ZugferdApiDelegate
import invoice.management.system.model.ZugferdInvoiceDto
import invoice.management.system.services.invoiceGeneration.mapper.toZugferdDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ZugferdService(
    private val zugferdManager: ZugferdManager,
) : ZugferdApiDelegate {

    override fun createZugferdInvoice(cardmarketExternalOrderId: Long): ResponseEntity<ZugferdInvoiceDto> {
        return when (val result = zugferdManager.createAndSaveZugferdInvoice(cardmarketExternalOrderId)) {
            is ZugferdInvoiceResult.Success ->
                ResponseEntity.status(HttpStatus.CREATED).body(result.invoice.toZugferdDto())
            is ZugferdInvoiceResult.OrderNotFound ->
                ResponseEntity.notFound().build()
            is ZugferdInvoiceResult.AlreadyExists ->
                ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    override fun getZugferdInvoiceById(id: Long): ResponseEntity<ZugferdInvoiceDto> {
        val invoice = zugferdManager.findById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(invoice.toZugferdDto())
    }
}
