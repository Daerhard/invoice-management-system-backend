package invoice.management.system.controller

import invoice.management.system.api.InvoiceEmailApi
import invoice.management.system.api.InvoiceEmailApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class InvoiceEmailController(
    @Autowired private val delegate: InvoiceEmailApiDelegate
) : InvoiceEmailApi {
    override fun getDelegate(): InvoiceEmailApiDelegate = delegate
}
