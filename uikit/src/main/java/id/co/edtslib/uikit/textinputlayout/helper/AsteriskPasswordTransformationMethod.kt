package id.co.edtslib.uikit.textinputlayout.helper

import android.text.method.PasswordTransformationMethod
import android.view.View

class AsteriskPasswordTransformationMethod : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        return PasswordCharSequence(source)
    }

    private inner class PasswordCharSequence(private val source: CharSequence) : CharSequence {
        override fun get(index: Int): Char {
            return '*'
        }

        override val length: Int
            get() = source.length

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return source.subSequence(startIndex, endIndex)
        }
    }

    companion object {
        val transformationMethod = AsteriskPasswordTransformationMethod()
    }
}