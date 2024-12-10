package invoice.management.system.controller

import invoice.management.system.api.CsvImportApi
import invoice.management.system.api.CsvImportApiDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${openapi.invoice-management-system.base-path:}")
class CSVImportController(
    @Autowired private val delegate: CsvImportApiDelegate
) : CsvImportApi {

    override fun getDelegate(): CsvImportApiDelegate = delegate
}