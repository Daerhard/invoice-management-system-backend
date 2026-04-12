package invoice.management.system.controller

import invoice.management.system.api.CardmarketPurchasesApi
import invoice.management.system.api.CardmarketPurchasesApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class CardmarketPurchasesController(
    @Autowired private val delegate: CardmarketPurchasesApiDelegate
) : CardmarketPurchasesApi {
    override fun getDelegate(): CardmarketPurchasesApiDelegate = delegate
}
