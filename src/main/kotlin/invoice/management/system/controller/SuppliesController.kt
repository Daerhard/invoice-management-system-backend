package invoice.management.system.controller

import invoice.management.system.api.SuppliesApi
import invoice.management.system.api.SuppliesApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class SuppliesController(
    @Autowired private val delegate: SuppliesApiDelegate
) : SuppliesApi {

    override fun getDelegate(): SuppliesApiDelegate = delegate
}
