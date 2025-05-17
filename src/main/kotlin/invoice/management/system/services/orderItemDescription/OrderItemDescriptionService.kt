package invoice.management.system.services.orderItemDescription

class OrderItemDescriptionService {

    fun getDescriptionDetails(orderItemDescription: String): DescriptionDetail {
        val descriptionSplit = splitDescriptionDetail(orderItemDescription)
        val splitItemDetails = descriptionSplit.itemDetails.split("-")

        val itemCount = getItemCount(descriptionSplit.itemCountTitle)
        val itemTitle = getItemTitle(descriptionSplit.itemCountTitle)
        val itemName = getItemName(itemTitle)
        val itemKonamiSet = getKonamiSet(itemTitle)
        val itemNumber = getItemNumber(splitItemDetails)
        val itemRarity = getRarity(splitItemDetails)
        val itemCondition = getCondition(splitItemDetails)
        val itemLanguage = getLanguage(splitItemDetails)
        val itemEdition = getFirstEdition(splitItemDetails)
        val itemPrice = getPrice(splitItemDetails)

        return DescriptionDetail(
            articleCount = itemCount,
            productName = itemName,
            konamiSet = itemKonamiSet,
            productNumber = itemNumber,
            language = itemLanguage,
            condition = itemCondition,
            rarity = itemRarity,
            isFirstEdition = itemEdition,
            price = itemPrice,
            )
    }

    private fun splitDescriptionDetail(descriptionDetail: String): OrderItemDescriptionSplit {
        val lastParenIndex = descriptionDetail.lastIndexOf(")")

        val orderItemDescriptionSplit = OrderItemDescriptionSplit(
            if (lastParenIndex != -1) descriptionDetail.substring(0, lastParenIndex + 1) else "",
            if (lastParenIndex != -1 && lastParenIndex + 1 < descriptionDetail.length) descriptionDetail.substring(
                lastParenIndex + 1
            ).trimStart() else ""
        )
        return orderItemDescriptionSplit
    }

    private fun getItemCount(itemCountTitle: String): Int {
        return itemCountTitle.split(" ").first().removeSuffix("x").trim().toInt()
    }

    private fun getItemTitle(itemCountTitle: String): String {
        return itemCountTitle.substringAfter(" ").trim()
    }

    private fun getItemName(itemTitle: String): String {
        return itemTitle.split("(")[0].trim()
    }

    private fun getKonamiSet(itemTitle: String): String {
        var lastOpen = -1
        var openCount = 0

        for (i in itemTitle.indices.reversed()) {
            when (itemTitle[i]) {
                ')' -> {
                    if (openCount == 0) lastOpen = i
                    openCount++
                }

                '(' -> {
                    openCount--
                    if (openCount == 0 && lastOpen != -1) {
                        return itemTitle.substring(i, lastOpen + 1).replace("(", "").replace(")", "")
                    }
                }
            }
        }
        return ""
    }

    private fun getItemNumber(itemDetails: List<String>): String {
        val numberPart = itemDetails[1].trim()
        val numbersOnly = numberPart.filter { it.isDigit() }

        return numbersOnly
    }

    private fun getRarity(itemDetails: List<String>): String {
        return itemDetails[2].trim()
    }

    private fun getCondition(itemDetails: List<String>): String {
        return itemDetails[3].trim()
    }

    private fun getLanguage(itemDetails: List<String>): String {
        return itemDetails[4].trim()
    }

    private fun getFirstEdition(itemDetails: List<String>): Boolean {
        val firstEditionPart = itemDetails[5].trim()
        val isOnlyLetters = firstEditionPart.all { it.isLetter() }

        return isOnlyLetters
    }

    private fun getPrice(itemDetails: List<String>): Double {
        return itemDetails.last().replace(" ", "")
            .replace("EUR", "").replace(",", ".").toDouble()
    }
}

data class OrderItemDescriptionSplit(
    val itemCountTitle: String,
    val itemDetails: String,
)

data class DescriptionDetail(
    val articleCount: Int,
    val productName: String,
    val konamiSet: String,
    val productNumber: String,
    val language: String,
    val condition: String,
    val rarity: String,
    val isFirstEdition: Boolean,
    val price: Double
)