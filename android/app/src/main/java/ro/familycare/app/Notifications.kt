package ro.familycare.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import ro.familycare.core.CallAssessment
import ro.familycare.core.Messages

object Notifications {

    private const val CHANNEL_CALL_WARNINGS = "call_warnings"
    private const val CHANNEL_FAMILY_ALERTS = "family_alerts"
    private const val ID_CALL_WARNING = 1
    private const val ID_FAMILY_ALERT = 2

    fun createChannels(context: Context) {
        val manager = context.service<NotificationManager>()
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_CALL_WARNINGS, "Avertizări la apeluri", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_FAMILY_ALERTS, "Alerte pentru familie", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    /** Heads-up warning shown while an unknown number is ringing. */
    fun showCallWarning(context: Context, number: String?, assessment: CallAssessment) {
        createChannels(context)
        val settings = Settings(context)
        val title = Messages.warningTitle(assessment)
        val body = Messages.warningBody(assessment)

        val openWarning = PendingIntent.getActivity(
            context,
            0,
            WarningActivity.intent(context, title, body),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = Notification.Builder(context, CHANNEL_CALL_WARNINGS)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(number ?: "Număr ascuns")
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setCategory(Notification.CATEGORY_CALL)
            .setContentIntent(openWarning)
            .setAutoCancel(true)

        if (settings.trustedNumber.isNotBlank()) {
            val dialTrusted = PendingIntent.getActivity(
                context,
                1,
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + settings.trustedNumber))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, android.R.drawable.sym_action_call),
                    Messages.callTrustedPerson(settings.trustedName.ifBlank { "familia" }),
                    dialTrusted,
                ).build()
            )
        }

        context.service<NotificationManager>().notify(ID_CALL_WARNING, builder.build())
    }

    /** Local copy of a family alert, so the prototype shows something without SMS. */
    fun showFamilyAlert(context: Context, text: String) {
        createChannels(context)
        val notification = Notification.Builder(context, CHANNEL_FAMILY_ALERTS)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Alertă pentru familie")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .build()
        context.service<NotificationManager>().notify(ID_FAMILY_ALERT, notification)
    }
}
