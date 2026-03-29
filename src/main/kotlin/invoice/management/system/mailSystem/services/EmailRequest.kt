package invoice.management.system.mailSystem.services

import invoice.management.system.mailSystem.entities.EmailAttachment

data class EmailRequest(
    val to: String,
    val subject: String,
    val body: String,
    val isHtml: Boolean = false,
    val attachments: List<EmailAttachment> = emptyList(),
)
