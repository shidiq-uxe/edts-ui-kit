package id.co.edtslib.edtsuikit

import android.content.Context
import android.os.Bundle
import id.co.edtslib.uikit.core.linearprogressbar.CoreLinearProgressBar
import id.co.edtslib.uikit.recipes.linearprogressbar.klik.KlikLinearProgressBar
import id.co.edtslib.uikit.recipes.linearprogressbar.poinku.PoinkuLinearProgressBar

data class ProgressBarConfig(
    val product: ProgressBarProduct = ProgressBarProduct.KLIK,
    val mode: ProgressBarMode = ProgressBarMode.SINGLE_LAP,
    /** Percent of the bar's progressLimit. Only used by modes where [ProgressBarMode.usesProgress]. */
    val progress: Int = 50,
    val disabled: Boolean = false,
    val thicknessDp: Int? = null,
    val showBadge: Boolean? = null,

    val showThumb: Boolean? = null,
    val thumbSizeDp: Int? = null,

    val showInnerShadow: Boolean? = null,
    /** dp */
    val shadowOffsetX: Float? = null,
    /** dp */
    val shadowOffsetY: Float? = null,
    /** dp */
    val shadowSpread: Float? = null,

    // Solid colors. Indicator / thumb colors replace the product gradient.
    val indicatorColor: Int? = null,
    val thumbColor: Int? = null,
    val trackColor: Int? = null,
    /** Only meaningful in [ProgressBarMode.MULTI_LAP]. */
    val completedColor: Int? = null,

    val customColors: List<Int> = defaultColors,
) {
    /**
     * The preview is rebuilt only when this changes; everything else is applied to the live bar.
     * Overrides never go back to null on their own (only [withProduct] clears them, and that rebuilds),
     * so colors don't need to be part of the key.
     */
    val previewKey: String get() = "${product.name}|${mode.name}"

    /** A new product starts from its own defaults. */
    fun withProduct(product: ProgressBarProduct) = copy(
        product = product,
        thicknessDp = null,
        showBadge = null,
        showThumb = null,
        thumbSizeDp = null,
        showInnerShadow = null,
        shadowOffsetX = null,
        shadowOffsetY = null,
        shadowSpread = null,
        indicatorColor = null,
        thumbColor = null,
        trackColor = null,
        completedColor = null,
    )

    /** Multi lap turns the badge on by default, so a mode change drops the badge override. */
    fun withMode(mode: ProgressBarMode) = copy(mode = mode, showBadge = null)

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

enum class ProgressBarProduct(val label: String) {
    CORE("Core"),
    KLIK("Klik"),
    POINKU("Poinku");

    fun create(context: Context): CoreLinearProgressBar = when (this) {
        CORE -> CoreLinearProgressBar(context)
        KLIK -> KlikLinearProgressBar(context)
        POINKU -> PoinkuLinearProgressBar(context)
    }
}

enum class ProgressBarMode(
    val label: String,
    val usesProgress: Boolean,
    val hasAnimateAction: Boolean,
) {
    SINGLE_LAP("Single Lap", usesProgress = true, hasAnimateAction = true),
    MULTI_LAP("Multi Lap", usesProgress = true, hasAnimateAction = true),
    LOADING("Loading", usesProgress = false, hasAnimateAction = true),
    INTERMITTENT_FIXED("Intermittent Fixed", usesProgress = false, hasAnimateAction = false),
    INTERMITTENT_STRETCH("Intermittent Stretch", usesProgress = false, hasAnimateAction = false),
}

private const val CONFIG_PRODUCT = "product"
private const val CONFIG_MODE = "mode"
private const val CONFIG_PROGRESS = "progress"
private const val CONFIG_DISABLED = "disabled"
private const val CONFIG_THICKNESS = "thickness_dp"
private const val CONFIG_SHOW_BADGE = "show_badge"
private const val CONFIG_SHOW_THUMB = "show_thumb"
private const val CONFIG_THUMB_SIZE = "thumb_size_dp"
private const val CONFIG_SHOW_SHADOW = "show_inner_shadow"
private const val CONFIG_SHADOW_X = "shadow_offset_x"
private const val CONFIG_SHADOW_Y = "shadow_offset_y"
private const val CONFIG_SHADOW_SPREAD = "shadow_spread"
private const val CONFIG_INDICATOR_COLOR = "indicator_color"
private const val CONFIG_THUMB_COLOR = "thumb_color"
private const val CONFIG_TRACK_COLOR = "track_color"
private const val CONFIG_COMPLETED_COLOR = "completed_color"
private const val CONFIG_COLORS = "colors"

fun ProgressBarConfig.toBundle(): Bundle = Bundle().apply {
    putString(CONFIG_PRODUCT, product.name)
    putString(CONFIG_MODE, mode.name)
    putInt(CONFIG_PROGRESS, progress)
    putBoolean(CONFIG_DISABLED, disabled)
    // A null override is simply not written.
    thicknessDp?.let { putInt(CONFIG_THICKNESS, it) }
    showBadge?.let { putBoolean(CONFIG_SHOW_BADGE, it) }
    showThumb?.let { putBoolean(CONFIG_SHOW_THUMB, it) }
    thumbSizeDp?.let { putInt(CONFIG_THUMB_SIZE, it) }
    showInnerShadow?.let { putBoolean(CONFIG_SHOW_SHADOW, it) }
    shadowOffsetX?.let { putFloat(CONFIG_SHADOW_X, it) }
    shadowOffsetY?.let { putFloat(CONFIG_SHADOW_Y, it) }
    shadowSpread?.let { putFloat(CONFIG_SHADOW_SPREAD, it) }
    indicatorColor?.let { putInt(CONFIG_INDICATOR_COLOR, it) }
    thumbColor?.let { putInt(CONFIG_THUMB_COLOR, it) }
    trackColor?.let { putInt(CONFIG_TRACK_COLOR, it) }
    completedColor?.let { putInt(CONFIG_COMPLETED_COLOR, it) }
    putIntegerArrayList(CONFIG_COLORS, ArrayList(customColors))
}

fun Bundle.toProgressBarConfig(): ProgressBarConfig = ProgressBarConfig(
    product = getString(CONFIG_PRODUCT)
        ?.let { runCatching { ProgressBarProduct.valueOf(it) }.getOrNull() }
        ?: ProgressBarProduct.KLIK,
    mode = getString(CONFIG_MODE)
        ?.let { runCatching { ProgressBarMode.valueOf(it) }.getOrNull() }
        ?: ProgressBarMode.SINGLE_LAP,
    progress = getInt(CONFIG_PROGRESS, 50),
    disabled = getBoolean(CONFIG_DISABLED),
    thicknessDp = if (containsKey(CONFIG_THICKNESS)) getInt(CONFIG_THICKNESS) else null,
    showBadge = if (containsKey(CONFIG_SHOW_BADGE)) getBoolean(CONFIG_SHOW_BADGE) else null,
    showThumb = if (containsKey(CONFIG_SHOW_THUMB)) getBoolean(CONFIG_SHOW_THUMB) else null,
    thumbSizeDp = if (containsKey(CONFIG_THUMB_SIZE)) getInt(CONFIG_THUMB_SIZE) else null,
    showInnerShadow = if (containsKey(CONFIG_SHOW_SHADOW)) getBoolean(CONFIG_SHOW_SHADOW) else null,
    shadowOffsetX = if (containsKey(CONFIG_SHADOW_X)) getFloat(CONFIG_SHADOW_X) else null,
    shadowOffsetY = if (containsKey(CONFIG_SHADOW_Y)) getFloat(CONFIG_SHADOW_Y) else null,
    shadowSpread = if (containsKey(CONFIG_SHADOW_SPREAD)) getFloat(CONFIG_SHADOW_SPREAD) else null,
    indicatorColor = if (containsKey(CONFIG_INDICATOR_COLOR)) getInt(CONFIG_INDICATOR_COLOR) else null,
    thumbColor = if (containsKey(CONFIG_THUMB_COLOR)) getInt(CONFIG_THUMB_COLOR) else null,
    trackColor = if (containsKey(CONFIG_TRACK_COLOR)) getInt(CONFIG_TRACK_COLOR) else null,
    completedColor = if (containsKey(CONFIG_COMPLETED_COLOR)) getInt(CONFIG_COMPLETED_COLOR) else null,
    customColors = getIntegerArrayList(CONFIG_COLORS)?.toList() ?: ProgressBarConfig.defaultColors,
)