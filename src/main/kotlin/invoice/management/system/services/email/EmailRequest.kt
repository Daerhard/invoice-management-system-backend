package invoice.management.system.services.email

/**
 * Encapsulates all parameters needed to compose and send an e-mail.
 *
 * @property to         Recipient e-mail address.
 * @property subject    Subject line of the e-mail.
 * @property body       Body content; treated as plain text when [isHtml] is `false`, as HTML when `true`.
 * @property isHtml     When `true` the [body] is treated as HTML; defaults to `false`.
 * @property attachments Zero or more [EmailAttachment] instances to include in the mail.
 */
data class EmailRequest(
    val to: String,
    val subject: String,
    val body: String,
    val isHtml: Boolean = false,
    val attachments: List<EmailAttachment> = emptyList(),
)

/**
 * Represents a single file attachment.
 *
 * @property fileName    File name shown in the e-mail client (e.g. `"invoice.pdf"`).
 * @property content     Raw byte content of the file.
 * @property contentType MIME type of the file (e.g. `"application/pdf"`).
 */
data class EmailAttachment(
    val fileName: String,
    val content: ByteArray,
    val contentType: String = "application/pdf",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmailAttachment) return false
        return fileName == other.fileName &&
            contentType == other.contentType &&
            content.contentEquals(other.content)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}
