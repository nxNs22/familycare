package ro.familycare.core

enum class RiskLevel { NONE, CAUTION, HIGH }

enum class RiskReason {
    HIDDEN_NUMBER,
    UNKNOWN_NUMBER,
    INTERNATIONAL,
    PREMIUM_RATE,
    REPORTED_SCAM,
}

data class CallAssessment(val level: RiskLevel, val reasons: Set<RiskReason>) {
    val shouldWarn: Boolean get() = level != RiskLevel.NONE
}

/**
 * Decides whether an incoming call deserves a warning.
 *
 * It only looks at the number and whether it is a saved contact: no audio,
 * no call content. Calls are never blocked, only flagged.
 *
 * @param reportedScamNumbers numbers known to be used by scammers, in any format.
 */
class CallRiskEvaluator(reportedScamNumbers: Collection<String> = emptyList()) {

    private val reported: Set<String> =
        reportedScamNumbers.mapNotNull { PhoneNumbers.normalize(it) }.toSet()

    fun assess(rawNumber: String?, isContact: Boolean): CallAssessment {
        val number = PhoneNumbers.normalize(rawNumber)
            ?: return CallAssessment(RiskLevel.HIGH, setOf(RiskReason.HIDDEN_NUMBER))

        // A reported number wins over the contact list: scammers can spoof numbers.
        if (number in reported) {
            return CallAssessment(RiskLevel.HIGH, setOf(RiskReason.REPORTED_SCAM))
        }
        if (isContact) return CallAssessment(RiskLevel.NONE, emptySet())

        val reasons = mutableSetOf(RiskReason.UNKNOWN_NUMBER)
        if (PhoneNumbers.isInternational(number)) reasons += RiskReason.INTERNATIONAL
        if (PhoneNumbers.isPremiumRate(number)) reasons += RiskReason.PREMIUM_RATE

        val level = if (reasons.size > 1) RiskLevel.HIGH else RiskLevel.CAUTION
        return CallAssessment(level, reasons)
    }
}
