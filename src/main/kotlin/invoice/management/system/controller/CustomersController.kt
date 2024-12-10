package invoice.management.system.controller

import invoice.management.system.api.CustomersApi
import invoice.management.system.api.CustomersApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class CustomersController(
    @Autowired private val delegate: CustomersApiDelegate
) : CustomersApi {

    override fun getDelegate(): CustomersApiDelegate = delegate
}
