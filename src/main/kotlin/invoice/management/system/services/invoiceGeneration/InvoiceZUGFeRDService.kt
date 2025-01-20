package invoice.management.system.services.invoiceGeneration

import invoice.management.system.api.InvoiceGenerationZUGFeRDApiDelegate
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class InvoiceZUGFeRDService(

): InvoiceGenerationZUGFeRDApiDelegate {


    override fun getInvoiceZUGFeRD(cardmarketExternalOrderId: Long): ResponseEntity<Resource> {




        return super.getInvoiceZUGFeRD(cardmarketExternalOrderId)
    }
}