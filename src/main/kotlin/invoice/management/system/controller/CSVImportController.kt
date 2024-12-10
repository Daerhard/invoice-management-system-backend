package invoice.management.system.controller

import invoice.management.system.api.CSVImportApi
import invoice.management.system.api.CSVImportApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class CSVImportController(
    @Autowired private val delegate: CSVImportApiDelegate
) : CSVImportApi {

    override fun getDelegate(): CSVImportApiDelegate = delegate
}