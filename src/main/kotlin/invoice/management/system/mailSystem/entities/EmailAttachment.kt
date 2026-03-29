package invoice.management.system.mailSystem.entities

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