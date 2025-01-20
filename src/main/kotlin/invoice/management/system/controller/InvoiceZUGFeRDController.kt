package invoice.management.system.controller

import invoice.management.system.api.InvoiceGenerationZUGFeRDApi
import invoice.management.system.api.InvoiceGenerationZUGFeRDApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class InvoiceZUGFeRDController(
    @Autowired private val delegate: InvoiceGenerationZUGFeRDApiDelegate
) : InvoiceGenerationZUGFeRDApi {
    override fun getDelegate(): InvoiceGenerationZUGFeRDApiDelegate = delegate
}