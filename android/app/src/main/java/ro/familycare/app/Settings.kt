package ro.familycare.app

import android.content.Context
import ro.familycare.core.LongCallDetector

/** Prototype settings, kept on the device in SharedPreferences. */
class Settings(context: Context) {

    private val prefs = context.getSharedPreferences("familycare", Context.MODE_PRIVATE)

    var trustedName: String
        get() = prefs.getString(KEY_TRUSTED_NAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_TRUSTED_NAME, value.trim()).apply()

    var trustedNumber: String
        get() = prefs.getString(KEY_TRUSTED_NUMBER, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_TRUSTED_NUMBER, value.trim()).apply()

    var longCallMinutes: Int
        get() = prefs.getInt(KEY_LONG_CALL_MINUTES, (LongCallDetector.DEFAULT_THRESHOLD_SECONDS / 60).toInt())
        set(value) = prefs.edit().putInt(KEY_LONG_CALL_MINUTES, value.coerceAtLeast(1)).apply()

    /** Call-log timestamp of the last call we checked, so each call is handled once. */
    var lastCheckedCallMillis: Long
        get() = prefs.getLong(KEY_LAST_CHECKED_CALL, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CHECKED_CALL, value).apply()

    val history: List<String>
        get() = prefs.getString(KEY_HISTORY, "").orEmpty().lines().filter { it.isNotBlank() }

    fun addHistory(entry: String) {
        val updated = (listOf(entry) + history).take(MAX_HISTORY)
        prefs.edit().putString(KEY_HISTORY, updated.joinToString("\n")).apply()
    }

    private companion object {
        const val KEY_TRUSTED_NAME = "trusted_name"
        const val KEY_TRUSTED_NUMBER = "trusted_number"
        const val KEY_LONG_CALL_MINUTES = "long_call_minutes"
        const val KEY_LAST_CHECKED_CALL = "last_checked_call"
        const val KEY_HISTORY = "history"
        const val MAX_HISTORY = 30
    }
}
