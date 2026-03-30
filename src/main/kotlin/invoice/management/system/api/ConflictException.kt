package invoice.management.system.api

import org.springframework.http.HttpStatus

class ConflictException(msg: String, code: Int = HttpStatus.CONFLICT.value()) : ApiException(msg, code)
