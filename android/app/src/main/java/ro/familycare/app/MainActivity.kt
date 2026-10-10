package ro.familycare.app

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Setup screen: trusted person, permissions, call-screening role and recent events. */
class MainActivity : Activity() {

    private lateinit var settings: Settings
    private lateinit var status: TextView
    private lateinit var history: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(this)
        Notifications.createChannels(this)

        val nameInput = EditText(this).apply {
            hint = "Nume (ex. Andrei)"
            setText(settings.trustedName)
            textSize = 22f
        }
        val numberInput = EditText(this).apply {
            hint = "Telefon (ex. 0722 123 456)"
            inputType = InputType.TYPE_CLASS_PHONE
            setText(settings.trustedNumber)
            textSize = 22f
        }
        val minutesInput = EditText(this).apply {
            hint = "Minute"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(settings.longCallMinutes.toString())
            textSize = 22f
        }
        status = Ui.text(this, "", sizeSp = 18f)
        history = Ui.text(this, "", sizeSp = 16f)

        val layout = Ui.column(this).apply {
            addView(Ui.text(context, "FamilyCare", sizeSp = 32f, bold = true))
            addView(Ui.text(context, "Persoana de încredere", bold = true))
            addView(nameInput)
            addView(numberInput)
            addView(Ui.text(context, "Alertă după un apel necunoscut mai lung de (minute):"))
            addView(minutesInput)
            addView(Ui.bigButton(context, "Salvează", Ui.BLUE) {
                settings.trustedName = nameInput.text.toString()
                settings.trustedNumber = numberInput.text.toString()
                settings.longCallMinutes = minutesInput.text.toString().toIntOrNull() ?: settings.longCallMinutes
                Toast.makeText(context, "Salvat", Toast.LENGTH_SHORT).show()
            })
            addView(Ui.bigButton(context, "1. Acordă permisiunile", Ui.BLUE) { requestPermissions() })
            addView(Ui.bigButton(context, "2. Activează filtrarea apelurilor", Ui.BLUE) { requestScreeningRole() })
            addView(status)
            addView(Ui.text(context, "Evenimente recente", bold = true))
            addView(history)
        }
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
    }

    @Deprecated("Activity result API is not available without AndroidX")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refresh()
    }

    private fun requestPermissions() {
        val missing = requiredPermissions().filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), REQUEST_PERMISSIONS)
    }

    @Suppress("DEPRECATION")
    private fun requestScreeningRole() {
        val roles = service<RoleManager>()
        if (!roles.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            Toast.makeText(this, "Telefonul nu permite filtrarea apelurilor", Toast.LENGTH_LONG).show()
            return
        }
        if (!roles.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), REQUEST_ROLE)
        }
    }

    private fun refresh() {
        val roleHeld = service<RoleManager>().isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        val missing = requiredPermissions().count { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        status.text = buildString {
            append(if (missing == 0) "✓ Permisiuni acordate" else "✗ Lipsesc $missing permisiuni")
            append("\n")
            append(if (roleHeld) "✓ Filtrarea apelurilor este activă" else "✗ Filtrarea apelurilor nu este activă")
        }
        history.text = settings.history.ifEmpty { listOf("Niciun eveniment încă.") }.joinToString("\n")
    }

    private fun requiredPermissions(): List<String> = buildList {
        add(Manifest.permission.READ_CONTACTS)
        add(Manifest.permission.READ_PHONE_STATE)
        add(Manifest.permission.READ_CALL_LOG)
        add(Manifest.permission.SEND_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }

    private companion object {
        const val REQUEST_PERMISSIONS = 1
        const val REQUEST_ROLE = 2
    }
}
