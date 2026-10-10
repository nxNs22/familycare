package ro.familycare.app

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import ro.familycare.core.CallRiskEvaluator
import ro.familycare.core.PhoneNumbers
import java.text.DateFormat
import java.util.Date

/**
 * Called by Android for incoming calls once the app holds the call-screening
 * role. It never blocks or silences a call: it lets the call ring and posts a
 * warning when the caller looks risky.
 */
class ScamScreeningService : CallScreeningService() {

    // Prototype: an empty list. Later, a list maintained by the backend.
    private val evaluator = CallRiskEvaluator(reportedScamNumbers = emptyList())

    override fun onScreenCall(callDetails: Call.Details) {
        // Always answer first: Android expects a response within a few seconds.
        respondToCall(callDetails, CallResponse.Builder().build())

        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return

        val number = if (callDetails.handlePresentation == TelecomManager.PRESENTATION_ALLOWED) {
            callDetails.handle?.schemeSpecificPart
        } else {
            null
        }
        val assessment = evaluator.assess(number, Contacts.isContact(this, number))
        if (!assessment.shouldWarn) return

        Notifications.showCallWarning(this, number, assessment)
        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
        Settings(this).addHistory(
            "$time · avertizare ${assessment.level} · ${PhoneNumbers.normalize(number) ?: "număr ascuns"}"
        )
    }
}
