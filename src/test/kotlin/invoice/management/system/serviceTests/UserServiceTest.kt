package invoice.management.system.serviceTests

import invoice.management.system.factories.EntityFactory.Companion.createUser
import invoice.management.system.entities.User
import invoice.management.system.model.UserDto
import invoice.management.system.repositories.UserRepository
import invoice.management.system.services.user.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import java.util.Optional

class UserServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val userService = UserService(userRepository)

    @Test
    fun whenGetAllUsers_thenReturnUserList() {
        val user1 = createUser(username = "user1", email = "user1@example.com")
        val user2 = createUser(username = "user2", email = "user2@example.com")
        `when`(userRepository.findAll()).thenReturn(listOf(user1, user2))

        val response = userService.getAllUsers()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
    }

    @Test
    fun whenCreateUser_thenReturnCreatedUser() {
        val userDto = UserDto(
            username = "newuser",
            password = "secret",
            firstName = "Jane",
            lastName = "Doe",
            zipCode = "54321",
            city = "Munich",
            street = "Baker Street 2",
            email = "jane.doe@example.com",
        )
        val savedUser = createUser(
            username = "newuser",
            password = "secret",
            firstName = "Jane",
            lastName = "Doe",
            zipCode = "54321",
            city = "Munich",
            street = "Baker Street 2",
            email = "jane.doe@example.com",
        )
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        val response = userService.createUser(userDto)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("newuser", response.body?.username)
        assertEquals("jane.doe@example.com", response.body?.email)
        assertEquals(null, response.body?.password)
    }

    @Test
    fun whenCreateUser_withMissingPassword_thenReturn400() {
        val userDto = UserDto(
            username = "newuser",
            firstName = "Jane",
            lastName = "Doe",
            zipCode = "54321",
            city = "Munich",
            street = "Baker Street 2",
            email = "jane.doe@example.com",
        )

        val response = userService.createUser(userDto)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun whenGetUserById_withExistingId_thenReturnUser() {
        val user = createUser()
        user.id = 1
        `when`(userRepository.findById(1)).thenReturn(Optional.of(user))

        val response = userService.getUserById(1)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(user.username, response.body?.username)
    }

    @Test
    fun whenGetUserById_withNonExistingId_thenReturn404() {
        `when`(userRepository.findById(99)).thenReturn(Optional.empty())

        val response = userService.getUserById(99)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
