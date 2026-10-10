package ro.familycare.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager

/**
 * Wakes up when the phone goes back to idle (a call ended) and checks the
 * newest call-log entry. The delay gives the dialer time to write that entry.
 */
class CallEndedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) != TelephonyManager.EXTRA_STATE_IDLE) return

        val pending = goAsync()
        val appContext = context.applicationContext
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                CallLogChecker.checkLatestCall(appContext)
            } finally {
                pending.finish()
            }
        }, CALL_LOG_DELAY_MILLIS)
    }

    private companion object {
        const val CALL_LOG_DELAY_MILLIS = 3_000L
    }
}
