package invoice.management.system.controller

import invoice.management.system.api.EuerApi
import invoice.management.system.api.EuerApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class EuerController(
    @Autowired private val delegate: EuerApiDelegate
) : EuerApi {

    override fun getDelegate(): EuerApiDelegate = delegate
}
