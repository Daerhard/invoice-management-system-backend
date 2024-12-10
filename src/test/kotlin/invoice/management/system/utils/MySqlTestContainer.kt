package invoice.management.system.utils

import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer

interface MySQLTestContainer {

    companion object {
        @JvmStatic
        val mySQLContainer = MySQLContainer("mysql:8.0.33").apply {
            withDatabaseName("invoice-management-system-db")
            withUrlParam("createDatabaseIfNotExist", "true")
            withUrlParam("characterEncoding", "utf8")
            withUrlParam("useUnicode", "true")
            withUrlParam("useJDBCCompliantTimezoneShift", "true")
            withUrlParam("useLegacyDatetimeCode", "false")
            withUrlParam("serverTimezone", "UTC")
            withEnv("TZ", "UTC")
        }

        @JvmStatic
        @BeforeAll
        fun startDb() {
            if (!mySQLContainer.isCreated) {
                println(" >>> Starting MySQL container.")
                mySQLContainer.start()
            }
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            println(" >>> Setting up DB properties.")
            registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mySQLContainer::getUsername)
            registry.add("spring.datasource.password", mySQLContainer::getPassword)
            registry.add("spring.datasource.driver-class-name", mySQLContainer::getDriverClassName)
            registry.add("spring.flyway.enabled") { "true" }
        }
    }
}
