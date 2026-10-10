package ro.familycare.app

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Small helpers for building large, senior-friendly views in code. */
internal object Ui {

    fun column(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        val padding = dp(context, 24)
        setPadding(padding, padding, padding, padding)
    }

    fun text(context: Context, value: String, sizeSp: Float = 20f, bold: Boolean = false): TextView =
        TextView(context).apply {
            text = value
            setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
            setTextColor(Color.BLACK)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(context, 8), 0, dp(context, 8))
        }

    fun bigButton(context: Context, label: String, color: Int, onClick: () -> Unit): Button =
        Button(context).apply {
            text = label
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            minHeight = dp(context, 72)
            layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(context, 16)
            }
            setOnClickListener { onClick() }
        }

    fun dp(context: Context, value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()

    const val GREEN = 0xFF2E7D32.toInt()
    const val BLUE = 0xFF1565C0.toInt()
    const val GREY = 0xFF616161.toInt()
}

/** System services used here always exist on a phone (minSdk 29 with telephony). */
internal inline fun <reified T : Any> Context.service(): T =
    checkNotNull(getSystemService(T::class.java)) { "Missing system service ${T::class.java.simpleName}" }
