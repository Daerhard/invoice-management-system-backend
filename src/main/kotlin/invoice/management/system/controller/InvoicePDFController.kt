package invoice.management.system.controller

import invoice.management.system.api.InvoiceGenerationPDFApi
import invoice.management.system.api.InvoiceGenerationPDFApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class InvoiceGenerationPDF(
    @Autowired private val delegate: InvoiceGenerationPDFApiDelegate
) : InvoiceGenerationPDFApi {
    override fun getDelegate(): InvoiceGenerationPDFApiDelegate = delegate
}