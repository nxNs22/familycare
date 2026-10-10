package ro.familycare.core

/** A finished call as read from the call log. */
data class CompletedCall(
    val number: String?,
    val isContact: Boolean,
    val durationSeconds: Long,
    val endedAtMillis: Long,
)

/**
 * Flags long calls with unknown numbers, in either direction. Scammers often
 * keep a victim on the line, or ask them to call back, while they "verify"
 * a bank account or arrange a payment.
 */
class LongCallDetector(val thresholdSeconds: Long = DEFAULT_THRESHOLD_SECONDS) {

    fun shouldAlertFamily(call: CompletedCall): Boolean =
        !call.isContact && call.durationSeconds >= thresholdSeconds

    companion object {
        const val DEFAULT_THRESHOLD_SECONDS = 10L * 60
    }
}
