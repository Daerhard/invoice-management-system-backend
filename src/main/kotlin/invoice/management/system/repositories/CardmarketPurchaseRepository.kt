package invoice.management.system.repositories

import invoice.management.system.entities.CardmarketPurchase
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardmarketPurchaseRepository : CrudRepository<CardmarketPurchase, Int> {

    fun findByExternalOrderId(externalOrderId: Long): CardmarketPurchase?

    @EntityGraph(attributePaths = ["purchaseItems", "purchaseItems.card"])
    override fun findAll(): List<CardmarketPurchase>

    @EntityGraph(attributePaths = ["purchaseItems", "purchaseItems.card"])
    fun findBySellerUserName(sellerUserName: String): List<CardmarketPurchase>
}
