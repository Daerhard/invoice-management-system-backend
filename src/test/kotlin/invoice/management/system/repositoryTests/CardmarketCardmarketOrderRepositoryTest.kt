package invoice.management.system.repositoryTests

import invoice.management.system.factories.EntityFactory.Companion.createCard
import invoice.management.system.factories.EntityFactory.Companion.createCardmarketOrder
import invoice.management.system.factories.EntityFactory.Companion.createCustomer
import invoice.management.system.factories.EntityFactory.Companion.createOrderItem
import invoice.management.system.utils.RepositoryTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardmarketCardmarketOrderRepositoryTest : RepositoryTest() {

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

        val fetchedOrder = cardmarketOrderRepository.findByExternalOrderId(expectedOrder.externalOrderId)
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

    @Test
    fun whenFindAllWithAssociations_thenReturnAllOrdersWithDetails() {
        val customer = entityManager.persist(createCustomer())
        val order = entityManager.persist(createCardmarketOrder(customer = customer, orderItems = mutableListOf()))
        val card = entityManager.persist(createCard())
        val orderItem = createOrderItem(cardmarketOrder = order, card = card)
        order.orderItems.add(orderItem)
        entityManager.flushAndClear()

        val fetchedOrders = cardmarketOrderRepository.findAll()

        assertEquals(1, fetchedOrders.size)
        val fetchedOrder = fetchedOrders.first()
        assertEquals(order.externalOrderId, fetchedOrder.externalOrderId)
        assertEquals(customer.userName, fetchedOrder.customer.userName)
        assertEquals(1, fetchedOrder.orderItems.size)
        assertEquals(card, fetchedOrder.orderItems.first().card)
    }

    @Test
    fun whenFindAllWithAssociations_thenReturnAllOrdersForMultipleCustomers() {
        val customer1 = entityManager.persist(createCustomer(userName = "customer1"))
        val customer2 = entityManager.persist(createCustomer(userName = "customer2"))
        entityManager.persist(createCardmarketOrder(customer = customer1, orderId = 1001L, orderItems = mutableListOf()))
        entityManager.persist(createCardmarketOrder(customer = customer2, orderId = 1002L, orderItems = mutableListOf()))
        entityManager.flushAndClear()

        val fetchedOrders = cardmarketOrderRepository.findAll()

        assertEquals(2, fetchedOrders.size)
    }

    @Test
    fun whenFindByCustomerUserName_thenReturnOnlyMatchingOrders() {
        val customer1 = entityManager.persist(createCustomer(userName = "customer1"))
        val customer2 = entityManager.persist(createCustomer(userName = "customer2"))
        val order1 = entityManager.persist(createCardmarketOrder(customer = customer1, orderId = 2001L, orderItems = mutableListOf()))
        entityManager.persist(createCardmarketOrder(customer = customer2, orderId = 2002L, orderItems = mutableListOf()))
        val card = entityManager.persist(createCard())
        val orderItem = createOrderItem(cardmarketOrder = order1, card = card)
        order1.orderItems.add(orderItem)
        entityManager.flushAndClear()

        val fetchedOrders = cardmarketOrderRepository.findByCustomerUserName("customer1")

        assertEquals(1, fetchedOrders.size)
        val fetchedOrder = fetchedOrders.first()
        assertEquals(order1.externalOrderId, fetchedOrder.externalOrderId)
        assertEquals(customer1.userName, fetchedOrder.customer.userName)
        assertEquals(1, fetchedOrder.orderItems.size)
        assertEquals(card, fetchedOrder.orderItems.first().card)
    }

    @Test
    fun whenFindByCustomerUserName_thenReturnEmptyListForUnknownUser() {
        val customer = entityManager.persist(createCustomer(userName = "existingUser"))
        entityManager.persist(createCardmarketOrder(customer = customer, orderId = 3001L, orderItems = mutableListOf()))
        entityManager.flushAndClear()

        val fetchedOrders = cardmarketOrderRepository.findByCustomerUserName("nonExistentUser")

        assertTrue(fetchedOrders.isEmpty())
    }
}