package id.co.edtslib.uikit.button

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleRes
import com.google.android.material.button.MaterialButton
import id.co.edtslib.uikit.core.button.ButtonStyleResolver

// Todo : Refactor needed ASAP, Since some of MaterialButton Attrs is flagged as private.
object ButtonAttrsFactory {

    @SuppressLint("ResourceType", "PrivateResource")
    fun applyAttributes(
        context: Context,
        button: MaterialButton,
        @StyleRes styleRes: Int,
        attrs: AttributeSet? = null
    ) {
        ButtonStyleResolver.applyAttributes(
            context = context,
            button = button,
            styleRes = styleRes,
            attrs = attrs
        )
    }
}
