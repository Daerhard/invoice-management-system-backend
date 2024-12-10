package invoice.management.system.controller

import invoice.management.system.api.OrdersApi
import invoice.management.system.api.OrdersApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class OrderController(
    @Autowired private val delegate: OrdersApiDelegate
) : OrdersApi {
    override fun getDelegate(): OrdersApiDelegate = delegate
}
