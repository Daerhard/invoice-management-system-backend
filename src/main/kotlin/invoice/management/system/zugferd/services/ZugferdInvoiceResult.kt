package invoice.management.system.zugferd.services

import invoice.management.system.entities.Invoice

sealed class ZugferdInvoiceResult {
    data class Success(val invoice: Invoice) : ZugferdInvoiceResult()
    data class OrderNotFound(val message: String) : ZugferdInvoiceResult()
    data class AlreadyExists(val message: String) : ZugferdInvoiceResult()
}
