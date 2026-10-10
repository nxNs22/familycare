package ro.familycare.core

/** Phone number helpers tuned for Romanian numbering. */
object PhoneNumbers {

    /**
     * Normalizes a dialled or displayed number to "+<country><number>".
     *
     * Romanian national format ("0722 123 456") becomes "+40722123456" and the
     * "00" international prefix becomes "+". Short codes such as "112" or
     * "1880" are returned as plain digits. Returns null for a missing or empty
     * number (hidden caller).
     */
    fun normalize(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        val digits = trimmed.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return when {
            trimmed.startsWith("+") -> "+$digits"
            digits.startsWith("00") -> "+" + digits.drop(2)
            digits.startsWith("0") -> "+40" + digits.drop(1)
            else -> digits
        }
    }

    fun isRomanian(normalized: String): Boolean = normalized.startsWith("+40")

    fun isInternational(normalized: String): Boolean =
        normalized.startsWith("+") && !isRomanian(normalized)

    /** Romanian premium-rate ranges: 0900, 0903, 0906. */
    fun isPremiumRate(normalized: String): Boolean =
        PREMIUM_PREFIXES.any { normalized.startsWith(it) }

    private val PREMIUM_PREFIXES = listOf("+40900", "+40903", "+40906")
}
