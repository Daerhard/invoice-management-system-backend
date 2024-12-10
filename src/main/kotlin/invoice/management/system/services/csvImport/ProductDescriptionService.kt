package invoice.management.system.services.csvImport

import org.springframework.stereotype.Service

//"3x Purrelyly - Traskl (Cyberstorm Access) - 018 - Common - NM - German - FirstEd - 0,10 EUR"
val standardRegex = Regex(
    """^\d+x\s[^()]+?\s\([^()]+\)\s-\s\d+\s-\s[^()]+?\s-\s[^()]+?\s-\s[^()]+?\s-\s\d+,\d+\s[A-Z]{3}$"""
)
//3x Mementotlan Angwitch (V.1 - Ultra Rare) (Valiant Smashers) - 005 - Ultra Rare - NM - German - FirstEd - 14,50 EUR
val differentCardVersionRegex = Regex(
    """^\d+x\s[^()]+?\s\([^()]+\)\s\([^()]+\)\s-\s\d+\s-\s[^()]+?\s-\s[^()]+?\s-\s[^()]+?\s-\s\d+,\d+\s[A-Z]{3}$"""
)
//2x Cursed Seal of the Forbidden Spell (Invasion of Chaos (25th Anniversary Edition)) - 049 - Common - NM - German - 0,10 EUR
val anniversaryEditionRegex = Regex(
    """^\d+x\s[^()]+?\s\([^()]+\s\([^()]+\)\)\s-\s\d+\s-\s[^()]+?\s-\s[^()]+?\s-\s[^()]+?\s-\s\d+,\d+\s[A-Z]{3}$"""
)

@Service
class ProductDescriptionService{

    fun convertDescription(description: String): DescriptionDetail {

        return when{
            description.matches(standardRegex) -> {
                convertStandardString(description)
            }

            description.matches(differentCardVersionRegex) -> {
                convertDifferentCardVersionString(description)
            }

            description.matches(anniversaryEditionRegex) -> {
                convertAnniversaryEdition(description)
            }

            else -> {
                throw IllegalArgumentException("Description: $description does not match format")
            }
        }
    }

    private fun convertStandardString(description: String): DescriptionDetail {
        val splitDescription = convertDescriptionToList(description)

        return createDescriptionDetail(splitDescription)
    }

    private fun convertDifferentCardVersionString(description: String): DescriptionDetail {
        val regex = Regex("""\s\([^)]+\)""")
        val modifiedDescription = description.replaceFirst(regex, "").trim()

        val splitDescription = convertDescriptionToList(modifiedDescription)

        return createDescriptionDetail(splitDescription)
    }

    private fun convertAnniversaryEdition(description: String): DescriptionDetail {
        val regex = Regex("""\(([^()]+)\s\(([^()]+)\)\)""")
        val modifiedDescription = description.replace(regex, "($1 $2)")

        val splitDescription = convertDescriptionToList(modifiedDescription)

        return createDescriptionDetail(splitDescription)
    }

    private fun convertDescriptionToList(description: String): List<String> {
        val isFirstEdition = description.contains("First")
        val splitDescription = if (description.split("(")[0].contains(" - ")) {
            val name = description.split("(")[0]
            val splitRest = description.split("(")[1].split(" -")
            listOf("$name (${splitRest[0]}") + splitRest.drop(1)
        } else {
            description.split(" - ")
        }

        return splitDescription.take(5) + isFirstEdition.toString() + splitDescription.last()
    }

    private fun createDescriptionDetail(splitDescription: List<String>): DescriptionDetail {
        val regex = Regex("""^\d+x\s""")
        val splitProductTitle = splitDescription[0].split(" (")

        val descriptionDetail = DescriptionDetail(
            productName = splitProductTitle[0].replace(regex, ""),
            konamiSet = splitProductTitle[1].trimEnd(')'),
            productNumber = splitDescription[1]
                .replace(" ", "")
                .replace("0", "")
                .toInt(),
            rarity = splitDescription[2],
            condition = splitDescription[3],
            language = splitDescription[4],
            isFirstEdition = splitDescription[5].contains("First"),
            price = extractAndConvertPrice(splitDescription[6])
        )

        return descriptionDetail
    }

    private fun extractAndConvertPrice(priceString: String): Double {
        val regex = Regex("""\d+,\d+""")
        val match = regex.find(priceString)!!.value
        return match.replace(",", ".").toDouble()
    }
}

data class DescriptionDetail(
    val productName: String,
    val konamiSet: String,
    val productNumber: Int,
    val language: String,
    val condition: String,
    val rarity: String,
    val isFirstEdition: Boolean,
    val price: Double
)

