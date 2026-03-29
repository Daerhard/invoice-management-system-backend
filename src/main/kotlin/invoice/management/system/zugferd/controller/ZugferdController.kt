package invoice.management.system.zugferd.controller

import invoice.management.system.api.ZugferdApi
import invoice.management.system.api.ZugferdApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class ZugferdController(
    @Autowired private val delegate: ZugferdApiDelegate,
) : ZugferdApi {
    override fun getDelegate(): ZugferdApiDelegate = delegate
}
