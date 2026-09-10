package id.co.edtslib.uikit.core.textbadge

abstract class BadgeVariant {
    abstract val shape: BadgeShape
    abstract val preset: BadgePreset
    open val palette: BadgePalette? get() = null
    open val disabledPalette: BadgePalette? get() = null
}