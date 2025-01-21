package id.co.edtslib.uikit.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannedString
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment

/**
 * Convert [String] to HexColor of [Int]
 */
val String.asColor: Int get() = Color.parseColor(this)

/**
 * Return Device Width Pixels of [Int], will return null when [Context] is Null
 */
val Context?.deviceWidth: Int get() = this?.resources?.displayMetrics?.widthPixels ?: 0

/**
 * Return Device Height Pixels of [Int], will return null when [Context] is Null
 */
val Context?.deviceHeight: Int get() = this?.resources?.displayMetrics?.heightPixels ?: 0

/**
 * Return Layout Inflater, will return null when [Context] is Null
 */
val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

/**
 * Return Font/Typeface from res directory, will return null when [Context] is Null
 * @return [Typeface]
 */
fun Context?.font(@FontRes fontRes: Int) =
    this?.let { ResourcesCompat.getFont(it, fontRes) }

/**
 * Return Image/Drawable from res directory, will return null when [Context] is Null
 * @return [Drawable]
 */
fun Context?.drawable(@DrawableRes drawableRes: Int) =
    this?.let { ContextCompat.getDrawable(it, drawableRes) } ?: ColorDrawable(Color.TRANSPARENT)

/**
 * Return Color from res directory of type [Int], will return null when [Context] is Null
 * @return Color[Int]
 */
fun Context?.color(@ColorRes colorRes: Int) =
    this?.let { ContextCompat.getColor(it, colorRes) } ?: Color.TRANSPARENT

/**
 * Return Gradient/ColorStateList from res directory, will return null when [Context] is Null
 * @return [ColorStateList]
 */
fun Context?.colorStateList(@ColorRes colorRes: Int) =
    this?.let { ContextCompat.getColorStateList(it, colorRes) }
        ?: ColorStateList.valueOf(Color.TRANSPARENT)

/**
 * Return Dimension from res directory, will return null when [Context] is Null
 * @return [Int]
 */
fun Context?.dimen(@DimenRes dimenRes: Int) =
    this?.resources?.getDimension(dimenRes) ?: 0f

/**
 * Return Dimension pixel from res directory, will return null when [Context] is Null
 * @return [Int]
 */
fun Context?.dimenPixelSize(@DimenRes dimenRes: Int) =
    this?.resources?.getDimensionPixelSize(dimenRes)

val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() }} ${Build.MODEL}"

// Todo : Fix Return Invocation
@Suppress("Unchecked_Cast")
inline fun <reified T: Any> Context.array(@ArrayRes arrayRes: Int): Array<T> = when(T::class) {
        Int::class -> resources.getIntArray(arrayRes).toTypedArray() as Array<T>
        String::class -> resources.getStringArray(arrayRes) as Array<T>
        CharSequence::class -> resources.getTextArray(arrayRes) as Array<T>
        else -> throw IllegalStateException("Unknown Array Class")
}

fun Context?.colorAttr(attributeId: Int): Int {
    val typedValue = TypedValue()
    this?.theme?.resolveAttribute(attributeId, typedValue, true)
    return if (typedValue.type == TypedValue.TYPE_REFERENCE)
        color(typedValue.resourceId)
    else
        typedValue.data
}

fun Fragment.getText(id: Int, vararg args: Any): CharSequence {
    val escapedArgs =
        args.map {
            if (it is String)
                TextUtils.htmlEncode(it)
            else
                it
        }.toTypedArray()

    val resource = SpannedString(getText(id))
    val htmlResource =
        HtmlCompat.toHtml(resource, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    val formattedHtml = String.format(htmlResource, *escapedArgs)
    return HtmlCompat.fromHtml(formattedHtml, HtmlCompat.FROM_HTML_MODE_COMPACT).trim()
}

val Int.px: Float get() = (this / Resources.getSystem().displayMetrics.density)
val Int.dp: Float get() = (this * Resources.getSystem().displayMetrics.density)
val Int.sp: Float get() = (this / Resources.getSystem().displayMetrics.scaledDensity)

val Float.px: Float get() = (this / Resources.getSystem().displayMetrics.density)
val Float.dp: Float get() = (this * Resources.getSystem().displayMetrics.density)
val Float.sp: Float get() = (this / Resources.getSystem().displayMetrics.scaledDensity)
