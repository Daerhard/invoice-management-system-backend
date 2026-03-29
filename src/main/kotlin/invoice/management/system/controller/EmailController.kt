package invoice.management.system.controller

import invoice.management.system.api.EmailApi
import invoice.management.system.api.EmailApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class EmailController(
    @Autowired private val delegate: EmailApiDelegate
) : EmailApi {

    override fun getDelegate(): EmailApiDelegate = delegate
}
