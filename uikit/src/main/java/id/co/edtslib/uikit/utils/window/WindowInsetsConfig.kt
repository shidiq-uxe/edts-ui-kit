package id.co.edtslib.uikit.utils.window

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

sealed class WindowInsetsConfig {

    abstract fun applyTo(view: View)

    object SystemBars : WindowInsetsConfig() {
        override fun applyTo(view: View) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
        }
    }

    object TopBottomOnly : WindowInsetsConfig() {
        override fun applyTo(view: View) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, bars.top, 0, bars.bottom)
                insets
            }
        }
    }

    object None : WindowInsetsConfig() {
        override fun applyTo(view: View) = Unit
    }

    class Custom(
        private val block: (view: View, insets: WindowInsetsCompat) -> WindowInsetsCompat
    ) : WindowInsetsConfig() {
        override fun applyTo(view: View) {
            ViewCompat.setOnApplyWindowInsetsListener(view, block)
        }
    }
}