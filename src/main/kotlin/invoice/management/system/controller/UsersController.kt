package invoice.management.system.controller

import invoice.management.system.api.UsersApi
import invoice.management.system.api.UsersApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class UsersController(
    @Autowired private val delegate: UsersApiDelegate
) : UsersApi {

    override fun getDelegate(): UsersApiDelegate = delegate
}
