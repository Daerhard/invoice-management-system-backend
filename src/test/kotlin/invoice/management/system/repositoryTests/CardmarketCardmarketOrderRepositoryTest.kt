package invoice.management.system.repositoryTests

import invoice.management.system.factories.EntityFactory.Companion.createCard
import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.factories.EntityFactory.Companion.createOrderItem
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardmarketOrderRepositoryTest : RepositoryTest() {

    @Test
    fun whenFindByUserName_thenReturnEntity() {
        val customer = entityManager.persist(createCustomer())
        val expectedOrder =
            entityManager.persist(createCardmarketOrder(
                customer = customer,
                orderItems = mutableListOf()
            )
            )
        val card = entityManager.persist(createCard())

        val orderItem = createOrderItem(
                cardmarketOrder = expectedOrder,
                card = card
            )

        expectedOrder.orderItems.add(orderItem)
        entityManager.flushAndClear()

        val fetchedOrder = orderRepository.findByExternalOrderId(expectedOrder.externalOrderId)
        assertEquals(expectedOrder.externalOrderId, fetchedOrder?.externalOrderId)
        assertEquals(expectedOrder.customer, fetchedOrder?.customer)
        assertEquals(expectedOrder.dateOfPayment, fetchedOrder?.dateOfPayment)
        assertEquals(expectedOrder.articleCount, fetchedOrder?.articleCount)
        assertEquals(expectedOrder.merchandiseValue, fetchedOrder?.merchandiseValue)
        assertEquals(expectedOrder.shipmentCost, fetchedOrder?.shipmentCost)
        assertEquals(expectedOrder.totalValue, fetchedOrder?.totalValue)
        assertEquals(expectedOrder.commission, fetchedOrder?.commission)
        assertEquals(expectedOrder.currency, fetchedOrder?.currency)

        val fetchedOrderItem = fetchedOrder?.orderItems?.first()
        assertEquals(orderItem.id, fetchedOrderItem?.id)
        assertEquals(orderItem.card, fetchedOrderItem?.card)
        assertEquals(orderItem.count, fetchedOrderItem?.count)
        assertEquals(orderItem.condition, fetchedOrderItem?.condition)
        assertEquals(orderItem.price, fetchedOrderItem?.price)
        assertEquals(orderItem.isFirstEdition, fetchedOrderItem?.isFirstEdition)
    }
}