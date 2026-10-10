package ro.familycare.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import ro.familycare.core.CompletedCall
import ro.familycare.core.LongCallDetector
import ro.familycare.core.Messages
import java.text.DateFormat
import java.util.Date

object CallLogChecker {

    fun checkLatestCall(context: Context) {
        if (context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) return

        val call = latestCall(context) ?: return
        val settings = Settings(context)
        // The phone-state broadcast can fire more than once per call.
        if (call.endedAtMillis <= settings.lastCheckedCallMillis) return
        settings.lastCheckedCallMillis = call.endedAtMillis

        val detector = LongCallDetector(thresholdSeconds = settings.longCallMinutes * 60L)
        if (!detector.shouldAlertFamily(call)) return

        val text = Messages.familyAlert(call)
        val sent = FamilyNotifier.send(context, text)
        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
        settings.addHistory("$time · alertă familie (${if (sent) "SMS trimis" else "doar local"}) · ${call.durationSeconds / 60} min")
    }

    private fun latestCall(context: Context): CompletedCall? {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.CACHED_NAME,
        )
        return context.contentResolver
            .query(CallLog.Calls.CONTENT_URI, projection, null, null, "${CallLog.Calls.DATE} DESC")
            ?.use { cursor ->
                if (!cursor.moveToFirst()) return null
                val number = cursor.getString(0)
                val durationSeconds = cursor.getLong(1)
                val startedAt = cursor.getLong(2)
                val cachedName = cursor.getString(3)
                CompletedCall(
                    number = number,
                    isContact = !cachedName.isNullOrBlank() || Contacts.isContact(context, number),
                    durationSeconds = durationSeconds,
                    endedAtMillis = startedAt + durationSeconds * 1000,
                )
            }
    }
}
