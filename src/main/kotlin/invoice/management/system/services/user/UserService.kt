package invoice.management.system.services.user

import invoice.management.system.api.UsersApiDelegate
import invoice.management.system.model.UserDto
import invoice.management.system.repositories.UserRepository
import invoice.management.system.services.invoiceGeneration.mapper.toDto
import invoice.management.system.services.invoiceGeneration.mapper.toEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) : UsersApiDelegate {

    override fun createUser(userDto: UserDto): ResponseEntity<UserDto> {
        if (userDto.password.isNullOrBlank()) {
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        }
        val user = userDto.toEntity()
        val saved = userRepository.save(user)
        return ResponseEntity(saved.toDto(), HttpStatus.CREATED)
    }

    override fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val users = userRepository.findAll()
        return ResponseEntity(users.map { it.toDto() }, HttpStatus.OK)
    }

    override fun getUserById(id: Int): ResponseEntity<UserDto> {
        val user = userRepository.findById(id).orElse(null)
            ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(user.toDto(), HttpStatus.OK)
    }
}
