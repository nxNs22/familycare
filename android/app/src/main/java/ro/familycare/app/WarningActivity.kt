package ro.familycare.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ScrollView
import ro.familycare.core.Messages

/** Full-screen version of the call warning, opened from the notification. */
class WarningActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val settings = Settings(this)
        val layout = Ui.column(this).apply {
            addView(Ui.text(context, intent.getStringExtra(EXTRA_TITLE).orEmpty(), sizeSp = 30f, bold = true))
            addView(Ui.text(context, intent.getStringExtra(EXTRA_BODY).orEmpty(), sizeSp = 22f))
            if (settings.trustedNumber.isNotBlank()) {
                addView(Ui.bigButton(context, Messages.callTrustedPerson(settings.trustedName.ifBlank { "familia" }), Ui.GREEN) {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + settings.trustedNumber)))
                    finish()
                })
            }
            addView(Ui.bigButton(context, "Am înțeles", Ui.GREY) { finish() })
        }
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_BODY = "body"

        fun intent(context: Context, title: String, body: String): Intent =
            Intent(context, WarningActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_BODY, body)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
