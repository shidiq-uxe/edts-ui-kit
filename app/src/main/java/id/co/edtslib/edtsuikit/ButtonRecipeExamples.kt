package id.co.edtslib.edtsuikit

import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import id.co.edtslib.uikit.R as UIKitR

internal data class ButtonRecipeExample(
    @StyleRes val recipe: Int,
    @StringRes val variantLabel: Int,
    @StringRes val sizeLabel: Int,
    val isIconOnly: Boolean,
    val hasIcons: Boolean = true
) {
    val sizeFilter: SizeFilter
        get() = when (sizeLabel) {
            R.string.button_recipe_small, R.string.button_recipe_small16,
            R.string.button_recipe_small24 -> SizeFilter.SMALL
            R.string.button_recipe_medium -> SizeFilter.MEDIUM
            R.string.button_recipe_large, R.string.button_recipe_large16,
            R.string.button_recipe_large24 -> SizeFilter.LARGE
            else -> SizeFilter.ALL
        }
}

internal object ButtonRecipeExamples {

    val core = listOf(
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_CoreButton,
            R.string.button_recipe_label_only,
            R.string.button_recipe_default,
            false,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_CoreButton,
            R.string.button_recipe_with_icons,
            R.string.button_recipe_default,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_CoreButton,
            R.string.guidelines_neutral,
            R.string.button_recipe_default,
            true
        )
    )

    val klik = listOf(
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Primary_Small,
            R.string.button_recipe_primary,
            R.string.button_recipe_small,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Primary_Medium,
            R.string.button_recipe_primary,
            R.string.button_recipe_medium,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Primary_Large,
            R.string.button_recipe_primary,
            R.string.button_recipe_large,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Secondary_Small,
            R.string.button_recipe_secondary,
            R.string.button_recipe_small,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Secondary_Medium,
            R.string.button_recipe_secondary,
            R.string.button_recipe_medium,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Secondary_Large,
            R.string.button_recipe_secondary,
            R.string.button_recipe_large,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Tertiary_Small,
            R.string.button_recipe_tertiary,
            R.string.button_recipe_small,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Tertiary_Medium,
            R.string.button_recipe_tertiary,
            R.string.button_recipe_medium,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Tertiary_Large,
            R.string.button_recipe_tertiary,
            R.string.button_recipe_large,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Primary_Small,
            R.string.button_recipe_primary,
            R.string.button_recipe_small,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Primary_Medium,
            R.string.button_recipe_primary,
            R.string.button_recipe_medium,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Primary_Large,
            R.string.button_recipe_primary,
            R.string.button_recipe_large,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Secondary_Small,
            R.string.button_recipe_secondary,
            R.string.button_recipe_small,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Secondary_Medium,
            R.string.button_recipe_secondary,
            R.string.button_recipe_medium,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Secondary_Large,
            R.string.button_recipe_secondary,
            R.string.button_recipe_large,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Tertiary_Small,
            R.string.button_recipe_tertiary,
            R.string.button_recipe_small,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Tertiary_Medium,
            R.string.button_recipe_tertiary,
            R.string.button_recipe_medium,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Icon_Tertiary_Large,
            R.string.button_recipe_tertiary,
            R.string.button_recipe_large,
            true
        )
    )

    val poinku = listOf(
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Primary_Small,
            R.string.button_recipe_primary,
            R.string.button_recipe_small,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Primary_Medium,
            R.string.button_recipe_primary,
            R.string.button_recipe_medium,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Primary_Large,
            R.string.button_recipe_primary,
            R.string.button_recipe_large,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Outline_Small,
            R.string.button_recipe_outline,
            R.string.button_recipe_small,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Outline_Medium,
            R.string.button_recipe_outline,
            R.string.button_recipe_medium,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Outline_Large,
            R.string.button_recipe_outline,
            R.string.button_recipe_large,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Text_Small,
            R.string.button_recipe_text,
            R.string.button_recipe_small,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Text_Medium,
            R.string.button_recipe_text,
            R.string.button_recipe_medium,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Text_Large,
            R.string.button_recipe_text,
            R.string.button_recipe_large,
            false
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Primary_Small16,
            R.string.button_recipe_primary,
            R.string.button_recipe_small16,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Primary_Large16,
            R.string.button_recipe_primary,
            R.string.button_recipe_large16,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Primary_Small24,
            R.string.button_recipe_primary,
            R.string.button_recipe_small24,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Primary_Large24,
            R.string.button_recipe_primary,
            R.string.button_recipe_large24,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Outline_Small16,
            R.string.button_recipe_outline,
            R.string.button_recipe_small16,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Outline_Large16,
            R.string.button_recipe_outline,
            R.string.button_recipe_large16,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Outline_Small24,
            R.string.button_recipe_outline,
            R.string.button_recipe_small24,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Outline_Large24,
            R.string.button_recipe_outline,
            R.string.button_recipe_large24,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Shadow_Small16,
            R.string.button_recipe_shadow,
            R.string.button_recipe_small16,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Shadow_Large16,
            R.string.button_recipe_shadow,
            R.string.button_recipe_large16,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Shadow_Small24,
            R.string.button_recipe_shadow,
            R.string.button_recipe_small24,
            true
        ),
        ButtonRecipeExample(
            UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Icon_Shadow_Large24,
            R.string.button_recipe_shadow,
            R.string.button_recipe_large24,
            true
        )
    )
}
