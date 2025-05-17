package invoice.management.system.csvImportTests

import invoice.management.system.services.orderItemDescription.OrderItemDescriptionService
import org.junit.jupiter.api.Test

class OrderItemDescriptionServiceTest {

    private val orderItemDescriptionService = OrderItemDescriptionService()

    @Test
    fun test1(){
        val testDescription = "1x Pressured Planet Wraitsoth (V.1 - Super Rare) (25th Anniversary Rarity Collection II) - 073 - Super Rare - NM - German - FirstEd - 0,50 EUR"
        orderItemDescriptionService.getDescriptionDetails(testDescription)

    }


}