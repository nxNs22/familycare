package ro.familycare.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager

/**
 * Prototype transport for family alerts: an SMS to the trusted person, plus a
 * local notification. The real app will send alerts through its backend to
 * the family dashboard instead (Google Play restricts SEND_SMS).
 */
object FamilyNotifier {

    /** Returns true if an SMS was sent. */
    fun send(context: Context, text: String): Boolean {
        Notifications.showFamilyAlert(context, text)

        val number = Settings(context).trustedNumber
        if (number.isBlank()) return false
        if (context.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return false

        val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java) ?: return false
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
        sms.sendMultipartTextMessage(number, null, sms.divideMessage(text), null, null)
        return true
    }
}
