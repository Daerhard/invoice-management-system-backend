package invoice.management.system.controller

import invoice.management.system.api.RefundsApi
import invoice.management.system.api.RefundsApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class RefundsController(
    @Autowired private val delegate: RefundsApiDelegate
) : RefundsApi {

    override fun getDelegate(): RefundsApiDelegate = delegate
}
