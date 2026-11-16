# Button

Use `KlikButton` or `PoinkuButton` for product styles. Use `CoreButton` for shared behavior with your own XML style. Requires a Material-compatible theme.

## XML

```xml
<id.co.edtslib.uikit.recipes.button.klik.KlikButton
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/btnContinue"
    style="@style/Widget.EDTS.UIKit.Button.Klik.Primary.Medium"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/continue_label"
    app:startIcon="@drawable/ic_action" />
```

Wrappers default to `Primary.Small`. Select a recipe with XML `style`, `app:buttonRecipe`, or Kotlin `buttonRecipe`.

```kotlin
import id.co.edtslib.uikit.R as UIKitR

binding.btnContinue.apply {
    buttonRecipe = UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Secondary_Large
    isEnabled = true
    setOnClickListener { onContinue() }
}
```

For a neutral button, supply a style through `app:buttonRecipe` or your theme's `coreButtonStyle`:

```xml
<id.co.edtslib.uikit.core.button.CoreButton
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/continue_label"
    app:buttonRecipe="@style/Widget.EDTS.UIKit.CoreButton" />
```

## Recipes

Text style: `Widget.EDTS.UIKit.Button.<Product>.<Variant>.<Size>`.

| Product | Variants | Sizes | Minimum heights |
|---|---|---|---|
| Klik | `Primary`, `Secondary`, `Tertiary` | `Small`, `Medium`, `Large` | 28 / 32 / 40dp |
| Poinku | `Primary`, `Outline`, `Text` | `Small`, `Medium`, `Large` | 24 / 32 / 40dp |

Icon style: `Widget.EDTS.UIKit.Button.<Product>.Icon.<Variant>.<Size>`.

| Product | Variants | Sizes: button / icon |
|---|---|---|
| Klik | `Primary`, `Secondary`, `Tertiary` | `Small`: 28/16dp; `Medium`: 32/16dp; `Large`: 40/24dp |
| Poinku | `Primary`, `Outline`, `Shadow` | `Small16`: 24/16dp; `Large16`: 28/16dp; `Small24`: 32/24dp; `Large24`: 40/24dp |

For icon-only buttons, leave `text` empty, set `startIcon`, and provide an action-specific `contentDescription`. Poinku Shadow uses native 2dp elevation; its blur differs from Figma.

## Configuration

| API | Usage |
|---|---|
| `buttonRecipe` | Reapplies recipe appearance; keeps content, listeners, and state. |
| `startIcon`, `endIcon` | Optional RTL-aware slots. Recipe controls size, spacing, and tint. |
| `isEnabled` | Disabled buttons ignore user clicks. |
| `isDestructive` | Klik destructive appearance. No Poinku destructive recipe. |
| `shouldShowShimmer` | Enables shimmer; disabling restores the original parent. Does not disable clicks. |
| `pressedScale` | Touch scale; product recipes use `1f`. |

Android handles pressed state. Use either slot icons or MaterialButton's single `icon` API on one button.

Explicit XML attributes override recipe defaults during inflation. Assigning `buttonRecipe` later replaces appearance overrides. Restore screen-owned content, recipe, and state after recreation.

Give buttons a minimum 48dp touch area through their host. Allow height growth for large fonts and long labels.

Klik recipes use the shared Inter fallback. Its glyph widths differ slightly from the previous component font, so intrinsic button widths can change.

## Migration

`id.co.edtslib.uikit.button.Button` is deprecated. Existing constructors, properties, `ButtonType` ordinals/XML values, resources, and `ButtonAttrsFactory` remain available.

Choose the intended product before replacing legacy usage:

| Legacy family | Klik | Poinku |
|---|---|---|
| Filled | `Primary` | `Primary` |
| Secondary | `Secondary` | `Outline` |
| Tertiary | `Tertiary` | No equivalent |
| Text | No equivalent | `Text` |

Keep the size suffix. Product migration changes appearance; it is not an automatic replacement.

## References

- Designs: [Klik](https://www.figma.com/design/Tpv7igrzhayWSY0z15u2RB/?node-id=4-2), [Poinku](https://www.figma.com/design/7e2kAWACnJlfcG0VONJJFP/?node-id=5-6).
- Shared font fallbacks: Inter 600 and Rubik 500. See [Typography](Typography.md#font-fallbacks). Licenses: [Inter](../uikit/src/main/assets/fonts/button_inter_license.txt), [Rubik](../uikit/src/main/assets/fonts/button_rubik_license.txt).
- Sample: **Button**. Tap the toolbar product subtitle to select Core, Klik, or Poinku. Use Configure for states, size filters, color/corner overrides, and Reset to defaults.
