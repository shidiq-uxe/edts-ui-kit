package id.co.edtslib.uikit.utils.typography

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

object FontUtils {

    private val fontCache = mutableMapOf<Int, Typeface>()

    fun Context?.loadFont(@FontRes fontRes: Int): Typeface {
        if (this == null) return Typeface.DEFAULT

        fontCache[fontRes]?.let { return it }

        val typeface = loadFontWithFallback(this, fontRes)
        fontCache[fontRes] = typeface
        return typeface
    }

    private fun loadFontWithFallback(context: Context, @FontRes fontRes: Int): Typeface {
        return try {
            ResourcesCompat.getFont(context, fontRes) ?: loadLocalFontSafely(context, fontRes)
        } catch (e: Resources.NotFoundException) {
            loadLocalFontSafely(context, fontRes)
        } catch (e: Exception) {
            loadLocalFontSafely(context, fontRes)
        }
    }

    private fun loadLocalFontSafely(context: Context, @FontRes fontRes: Int): Typeface {
        return try {
            val fontName = context.resources.getResourceEntryName(fontRes)
            val fallbackFontRes = when (fontName) {
                "inter_semibold" -> getLocalFontRes(context, "inter_semibold_fallback")
                "inter_medium" -> getLocalFontRes(context, "inter_medium_fallback")
                "inter_bold" -> getLocalFontRes(context, "inter_bold_fallback")
                "inter" -> getLocalFontRes(context, "inter_fallback")
                else -> 0
            }

            if (fallbackFontRes != 0) {
                ResourcesCompat.getFont(context, fallbackFontRes) ?: getSystemFallback(fontName)
            } else {
                getSystemFallback(fontName)
            }
        } catch (e: Exception) {
            getSystemFallback("default")
        }
    }

    private fun getLocalFontRes(context: Context, fontName: String): Int {
        return try {
            context.resources.getIdentifier(fontName, "font", context.packageName)
        } catch (e: Exception) {
            0
        }
    }

    private fun getSystemFallback(fontName: String): Typeface {
        return when (fontName) {
            "inter_semibold" -> Typeface.DEFAULT_BOLD
            "inter_bold" -> Typeface.DEFAULT_BOLD
            "inter_medium" -> Typeface.DEFAULT_BOLD
            "inter" -> Typeface.DEFAULT
            else -> Typeface.DEFAULT
        }
    }

    /**
     * Clear font cache when needed (e.g., on configuration changes)
     */
    fun clearCache() {
        fontCache.clear()
    }
}

