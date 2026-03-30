package invoice.management.system.mailSystem.services

sealed class InvoiceEmailWorkflowResult {
    data class Success(val recipient: String) : InvoiceEmailWorkflowResult()
    data class NotFound(val message: String) : InvoiceEmailWorkflowResult()
    data class Failed(val message: String) : InvoiceEmailWorkflowResult()
}
