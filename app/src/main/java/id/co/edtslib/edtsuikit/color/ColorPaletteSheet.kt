package id.co.edtslib.edtsuikit.color

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ColorPaletteSheet private constructor() {

    companion object {
        fun show(
            context: Context,
            title: String,
            selectedColor: Int?,
            onColorPicked: (Int) -> Unit,
        ) {
            val currentColor = selectedColor ?: Color.parseColor("#1178D4")

            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                val pad = 24.dp(context)
                setPadding(pad, pad, pad, pad)
            }

            val picker = HsvColorPicker(context).apply {
                setColor(currentColor)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300.dp(context)
                ).apply { bottomMargin = 16.dp(context) }
            }

            val previewContainer = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16.dp(context) }
            }

            val preview = View(context).apply {
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 8.dp(context).toFloat()
                    setColor(currentColor)
                }
                layoutParams = LinearLayout.LayoutParams(48.dp(context), 48.dp(context)).apply {
                    marginEnd = 12.dp(context)
                }
            }

            val hexText = TextView(context).apply {
                textSize = 14f
                text = String.format("#%06X", 0xFFFFFF and currentColor)
                setTextColor(Color.parseColor("#434755"))
            }

            previewContainer.addView(preview)
            previewContainer.addView(hexText)

            container.addView(picker)
            container.addView(previewContainer)

            picker.onColorChanged = { color ->
                (preview.background as? android.graphics.drawable.GradientDrawable)?.setColor(color)
                hexText.text = String.format("#%06X", 0xFFFFFF and color)
            }

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Done") { d, _ ->
                    onColorPicked(picker.getColor())
                    d.dismiss()
                }
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                .create()

            dialog.show()
        }

        private fun Int.dp(context: Context): Int =
            (this * context.resources.displayMetrics.density).toInt()
    }
}
