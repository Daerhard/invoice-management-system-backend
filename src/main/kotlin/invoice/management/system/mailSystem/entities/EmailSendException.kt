package invoice.management.system.mailSystem.entities

/**
 * Thrown by [EmailService] when an e-mail cannot be sent.
 *
 * Wraps lower-level Spring Mail / Jakarta Mail exceptions so that callers only
 * need to catch a single, domain-specific exception type.
 */
class EmailSendException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)