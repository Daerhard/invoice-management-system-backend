package invoice.management.system.repositories

import invoice.management.system.entities.Card
import invoice.management.system.entities.CardId
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository: CrudRepository<Card, CardId>