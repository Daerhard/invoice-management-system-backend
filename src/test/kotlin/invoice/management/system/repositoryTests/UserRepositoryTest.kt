package invoice.management.system.repositoryTests

import invoice.management.system.factories.EntityFactory.Companion.createUser
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest : RepositoryTest() {

    @Test
    fun whenSaveUser_thenReturnSavedEntityWithGeneratedId() {
        val user = createUser()
        val saved = userRepository.save(user)
        entityManager.flushAndClear()

        assertNotNull(saved.id)
        val fetched = userRepository.findById(saved.id).orElse(null)
        assertNotNull(fetched)
        assertEquals(user.username, fetched.username)
        assertEquals(user.firstName, fetched.firstName)
        assertEquals(user.lastName, fetched.lastName)
        assertEquals(user.zipCode, fetched.zipCode)
        assertEquals(user.city, fetched.city)
        assertEquals(user.street, fetched.street)
        assertEquals(user.email, fetched.email)
    }

    @Test
    fun whenFindAll_thenReturnAllUsers() {
        val user1 = createUser(username = "user1", email = "user1@example.com")
        val user2 = createUser(username = "user2", email = "user2@example.com")
        userRepository.save(user1)
        userRepository.save(user2)
        entityManager.flushAndClear()

        val users = userRepository.findAll()
        assertEquals(2, users.size)
    }
}
