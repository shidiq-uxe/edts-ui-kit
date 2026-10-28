package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.uikit.core.chip.CHIP_STATE_ACTIVE
import id.co.edtslib.uikit.core.chip.CHIP_STATE_IDLE
import id.co.edtslib.uikit.core.chip.CHIP_STATE_SELECTED
import id.co.edtslib.uikit.core.chip.ChipStateAnnotation
import id.co.edtslib.uikit.core.chip.ChipVariant
import id.co.edtslib.uikit.recipes.chip.klik.KlikChipVariant
import id.co.edtslib.uikit.recipes.chip.poinku.PoinkuChipVariant

data class ChipConfig(
    val product: ChipProduct = ChipProduct.CORE,
    val variantIndex: Int = 0,
    @ChipStateAnnotation val state: Int = CHIP_STATE_IDLE,
    val text: String = "Chip",
    val tintColor: Int? = null,
    val customColors: List<Int> = defaultColors,
    val isStartIconVisible: Boolean = false,
    val isStartIconActionable: Boolean = false,
    val isStartIconBackgroundVisible: Boolean? = null,
    val startIconBackgroundColor: Int? = null,
    val isEndIconVisible: Boolean = false,
    val isEndIconActionable: Boolean = false,
    val isEndIconBackgroundVisible: Boolean? = null,
    val endIconBackgroundColor: Int? = null,
    val textColor: Int? = null,
    val borderColor: Int? = null,
    val borderWidth: Int = 1,
    val startIconColor: Int? = null,
    val endIconColor: Int? = null,
) {
    fun stateLabels(): List<String> = listOf("Idle", "Active", "Selected")
    fun variantLabels(): List<String> = product.variantLabels

    fun variant(): ChipVariant? = when (product) {
        ChipProduct.CORE -> null
        ChipProduct.KLIK -> if (variantIndex == 1) KlikChipVariant.Filters else KlikChipVariant.Suggestion
        ChipProduct.POINKU -> PoinkuChipVariant
    }

    fun resetForVariant(product: ChipProduct = this.product, variantIndex: Int = 0, ): ChipConfig {
        return copy(
            product = product,
            variantIndex = variantIndex,
            state = CHIP_STATE_IDLE,
            text = "Chip",
            tintColor = null,
            customColors = defaultColors,
            isStartIconVisible = false,
            isStartIconActionable = false,
            isStartIconBackgroundVisible = null,
            startIconBackgroundColor = null,
            isEndIconVisible = false,
            isEndIconActionable = false,
            isEndIconBackgroundVisible = null,
            endIconBackgroundColor = null,
            textColor = null,
            borderColor = null,
            borderWidth = 1,
            startIconColor = null,
            endIconColor = null,
        )
    }

    companion object {
        val defaultColors = listOf(
            0xFF1659AB.toInt(),
            0xFFFFAB00.toInt(),
            0xFFFF3030.toInt(),
            0xFF1A9F67.toInt(),
            0xFF18A4F2.toInt(),
            0xFFED608F.toInt(),
        )
    }
}

enum class ChipProduct(val label: String, val variantLabels: List<String>) {
    CORE("Core", listOf("Default")),
    KLIK("Klik", listOf("Suggestion", "Filters")),
    POINKU("Poinku", listOf("Default")),
}

private const val CONFIG_PRODUCT = "product"
private const val CONFIG_VARIANT_INDEX = "variant_index"
private const val CONFIG_STATE = "state"
private const val CONFIG_TEXT = "text"
private const val CONFIG_HAS_TINT = "has_tint"
private const val CONFIG_TINT = "tint"
private const val CONFIG_COLORS = "colors"
private const val CONFIG_START_ICON_VISIBLE = "start_icon_visible"
private const val CONFIG_START_ICON_ACTIONABLE = "start_icon_actionable"
private const val CONFIG_HAS_START_ICON_BG_VISIBLE = "has_start_icon_bg_visible"
private const val CONFIG_START_ICON_BG_VISIBLE = "start_icon_bg_visible"
private const val CONFIG_HAS_START_ICON_BG_COLOR = "has_start_icon_bg_color"
private const val CONFIG_START_ICON_BG_COLOR = "start_icon_bg_color"
private const val CONFIG_END_ICON_VISIBLE = "end_icon_visible"
private const val CONFIG_END_ICON_ACTIONABLE = "end_icon_actionable"
private const val CONFIG_HAS_END_ICON_BG_VISIBLE = "has_end_icon_bg_visible"
private const val CONFIG_END_ICON_BG_VISIBLE = "end_icon_bg_visible"
private const val CONFIG_HAS_END_ICON_BG_COLOR = "has_end_icon_bg_color"
private const val CONFIG_END_ICON_BG_COLOR = "end_icon_bg_color"
private const val CONFIG_HAS_TEXT_COLOR = "has_text_color"
private const val CONFIG_TEXT_COLOR = "text_color"
private const val CONFIG_HAS_BORDER_COLOR = "has_border_color"
private const val CONFIG_BORDER_COLOR = "border_color"
private const val CONFIG_BORDER_WIDTH = "border_width"
private const val CONFIG_HAS_START_ICON_COLOR = "has_start_icon_color"
private const val CONFIG_START_ICON_COLOR = "start_icon_color"
private const val CONFIG_HAS_END_ICON_COLOR = "has_end_icon_color"
private const val CONFIG_END_ICON_COLOR = "end_icon_color"

fun ChipConfig.toBundle(): Bundle = Bundle().apply {
    putString(CONFIG_PRODUCT, product.name)
    putInt(CONFIG_VARIANT_INDEX, variantIndex)
    putInt(CONFIG_STATE, state)
    putString(CONFIG_TEXT, text)
    putBoolean(CONFIG_HAS_TINT, tintColor != null)
    tintColor?.let { putInt(CONFIG_TINT, it) }
    putIntegerArrayList(CONFIG_COLORS, ArrayList(customColors))
    putBoolean(CONFIG_START_ICON_VISIBLE, isStartIconVisible)
    putBoolean(CONFIG_START_ICON_ACTIONABLE, isStartIconActionable)
    putBoolean(CONFIG_HAS_START_ICON_BG_VISIBLE, isStartIconBackgroundVisible != null)
    isStartIconBackgroundVisible?.let { putBoolean(CONFIG_START_ICON_BG_VISIBLE, it) }
    putBoolean(CONFIG_HAS_START_ICON_BG_COLOR, startIconBackgroundColor != null)
    startIconBackgroundColor?.let { putInt(CONFIG_START_ICON_BG_COLOR, it) }
    putBoolean(CONFIG_END_ICON_VISIBLE, isEndIconVisible)
    putBoolean(CONFIG_END_ICON_ACTIONABLE, isEndIconActionable)
    putBoolean(CONFIG_HAS_END_ICON_BG_VISIBLE, isEndIconBackgroundVisible != null)
    isEndIconBackgroundVisible?.let { putBoolean(CONFIG_END_ICON_BG_VISIBLE, it) }
    putBoolean(CONFIG_HAS_END_ICON_BG_COLOR, endIconBackgroundColor != null)
    endIconBackgroundColor?.let { putInt(CONFIG_END_ICON_BG_COLOR, it) }
    putBoolean(CONFIG_HAS_TEXT_COLOR, textColor != null)
    textColor?.let { putInt(CONFIG_TEXT_COLOR, it) }
    putBoolean(CONFIG_HAS_BORDER_COLOR, borderColor != null)
    borderColor?.let { putInt(CONFIG_BORDER_COLOR, it) }
    putInt(CONFIG_BORDER_WIDTH, borderWidth)
    putBoolean(CONFIG_HAS_START_ICON_COLOR, startIconColor != null)
    startIconColor?.let { putInt(CONFIG_START_ICON_COLOR, it) }
    putBoolean(CONFIG_HAS_END_ICON_COLOR, endIconColor != null)
    endIconColor?.let { putInt(CONFIG_END_ICON_COLOR, it) }
}

fun Bundle.toChipConfig(): ChipConfig = ChipConfig(
    product = getString(CONFIG_PRODUCT)?.let { runCatching { ChipProduct.valueOf(it) }.getOrNull() } ?: ChipProduct.CORE,
    variantIndex = getInt(CONFIG_VARIANT_INDEX, 0),
    state = getInt(CONFIG_STATE, CHIP_STATE_IDLE),
    text = getString(CONFIG_TEXT).orEmpty(),
    tintColor = if (getBoolean(CONFIG_HAS_TINT)) getInt(CONFIG_TINT) else null,
    customColors = getIntegerArrayList(CONFIG_COLORS)?.toList() ?: ChipConfig.defaultColors,
    isStartIconVisible = getBoolean(CONFIG_START_ICON_VISIBLE),
    isStartIconActionable = getBoolean(CONFIG_START_ICON_ACTIONABLE),
    isStartIconBackgroundVisible = if (getBoolean(CONFIG_HAS_START_ICON_BG_VISIBLE)) getBoolean(CONFIG_START_ICON_BG_VISIBLE) else null,
    startIconBackgroundColor = if (getBoolean(CONFIG_HAS_START_ICON_BG_COLOR)) getInt(CONFIG_START_ICON_BG_COLOR) else null,
    isEndIconVisible = getBoolean(CONFIG_END_ICON_VISIBLE),
    isEndIconActionable = getBoolean(CONFIG_END_ICON_ACTIONABLE),
    isEndIconBackgroundVisible = if (getBoolean(CONFIG_HAS_END_ICON_BG_VISIBLE)) getBoolean(CONFIG_END_ICON_BG_VISIBLE) else null,
    endIconBackgroundColor = if (getBoolean(CONFIG_HAS_END_ICON_BG_COLOR)) getInt(CONFIG_END_ICON_BG_COLOR) else null,
    textColor = if (getBoolean(CONFIG_HAS_TEXT_COLOR)) getInt(CONFIG_TEXT_COLOR) else null,
    borderColor = if (getBoolean(CONFIG_HAS_BORDER_COLOR)) getInt(CONFIG_BORDER_COLOR) else null,
    borderWidth = getInt(CONFIG_BORDER_WIDTH, 1),
    startIconColor = if (getBoolean(CONFIG_HAS_START_ICON_COLOR)) getInt(CONFIG_START_ICON_COLOR) else null,
    endIconColor = if (getBoolean(CONFIG_HAS_END_ICON_COLOR)) getInt(CONFIG_END_ICON_COLOR) else null,
)

internal fun stateToIndex(@ChipStateAnnotation state: Int): Int = when (state) {
    CHIP_STATE_ACTIVE -> 1
    CHIP_STATE_SELECTED -> 2
    else -> 0
}

@ChipStateAnnotation
internal fun indexToState(index: Int): Int = when (index) {
    1 -> CHIP_STATE_ACTIVE
    2 -> CHIP_STATE_SELECTED
    else -> CHIP_STATE_IDLE
}