package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.uikit.button.Button.ButtonType

data class ButtonConfig(
    val destructive: Boolean = false,
    val disabled: Boolean = false,
    val shimmer: Boolean = false,
    val cornerRadius: Float = 8f,
    val sizeFilter: SizeFilter = SizeFilter.ALL,
    val tintColor: Int? = null,
    val customColors: List<Int> = defaultColors,
) {
    val visibleVariants: List<ButtonVariant>
        get() = when (sizeFilter) {
            SizeFilter.ALL -> allVariants
            else -> allVariants.filter { it.size == sizeFilter }
        }

    companion object {
        val defaultColors = listOf(
            0xFF1659AB.toInt(),
            0xFFFFAB00.toInt(),
            0xFFFF3030.toInt(),
            0xFF1A9F67.toInt(),
            0xFF18A4F2.toInt(),
            0xFFED608F.toInt(),
            0xFF191919.toInt(),
            0xFF878F99.toInt(),
        )

        private fun variant(section: Section, type: ButtonType, size: SizeFilter, label: String) =
            ButtonVariant(section, type, size, label)

        private fun sized(
            section: Section,
            baseName: String,
            small: ButtonType,
            medium: ButtonType,
            large: ButtonType,
        ) = listOf(
            variant(section, small, SizeFilter.SMALL, "Small $baseName Button"),
            variant(section, medium, SizeFilter.MEDIUM, "Medium $baseName Button"),
            variant(section, large, SizeFilter.LARGE, "Large $baseName Button"),
        )

        val allVariants: List<ButtonVariant> = buildList {
            addAll(sized(Section.FILLED, "Primary", ButtonType.FILLED, ButtonType.FILLED_MEDIUM, ButtonType.FILLED_LARGE))
            addAll(sized(Section.SECONDARY, "Secondary", ButtonType.SECONDARY, ButtonType.SECONDARY_MEDIUM, ButtonType.SECONDARY_LARGE))
            addAll(sized(Section.TERTIARY, "Tertiary", ButtonType.TERTIARY, ButtonType.TERTIARY_MEDIUM, ButtonType.TERTIARY_LARGE))
            addAll(sized(Section.TEXT, "Text", ButtonType.TEXT, ButtonType.TEXT_MEDIUM, ButtonType.TEXT_LARGE))
        }
    }
}

enum class SizeFilter { SMALL, MEDIUM, LARGE, ALL }

data class ButtonVariant(
    val section: Section,
    val type: ButtonType,
    val size: SizeFilter,
    val label: String,
)

enum class Section { FILLED, SECONDARY, TERTIARY, TEXT }

private const val CONFIG_DESTRUCTIVE = "destructive"
private const val CONFIG_DISABLED = "disabled"
private const val CONFIG_SHIMMER = "shimmer"
private const val CONFIG_CORNER_RADIUS = "corner_radius"
private const val CONFIG_SIZE_FILTER = "size_filter"
private const val CONFIG_HAS_TINT = "has_tint"
private const val CONFIG_TINT = "tint"
private const val CONFIG_COLORS = "colors"

fun ButtonConfig.toBundle(): Bundle = Bundle().apply {
    putBoolean(CONFIG_DESTRUCTIVE, destructive)
    putBoolean(CONFIG_DISABLED, disabled)
    putBoolean(CONFIG_SHIMMER, shimmer)
    putFloat(CONFIG_CORNER_RADIUS, cornerRadius)
    putString(CONFIG_SIZE_FILTER, sizeFilter.name)
    putBoolean(CONFIG_HAS_TINT, tintColor != null)
    tintColor?.let { putInt(CONFIG_TINT, it) }
    putIntegerArrayList(CONFIG_COLORS, ArrayList(customColors))
}

fun Bundle.toButtonConfig(): ButtonConfig = ButtonConfig(
    destructive = getBoolean(CONFIG_DESTRUCTIVE),
    disabled = getBoolean(CONFIG_DISABLED),
    shimmer = getBoolean(CONFIG_SHIMMER),
    cornerRadius = getFloat(CONFIG_CORNER_RADIUS, 8f),
    sizeFilter = getString(CONFIG_SIZE_FILTER)
        ?.let { runCatching { SizeFilter.valueOf(it) }.getOrNull() }
        ?: SizeFilter.ALL,
    tintColor = if (getBoolean(CONFIG_HAS_TINT)) getInt(CONFIG_TINT) else null,
    customColors = getIntegerArrayList(CONFIG_COLORS)?.toList() ?: ButtonConfig.defaultColors,
)
