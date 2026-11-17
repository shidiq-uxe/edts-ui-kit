# Snackbar and Toast recipes

Use Snackbar for brief in-app feedback. Klik and Poinku call their styled Snackbar a Toast. Each recipe shows an Info or Error message with an optional leading icon and a single optional action.

| Entry point | Message font | Action font |
| --- | --- | --- |
| `CoreSnackbar` | Inter Regular | Inter SemiBold |
| `KlikToast` | Inter Regular | Inter SemiBold |
| `PoinkuToast` | Rubik Regular | Rubik Medium |

## Variants

All three entry points use `CoreSnackbar.Variant`. The selected variant applies the corresponding XML theme overlay for Core, Klik, or Poinku.

| Variant | When to use | Background |
| --- | --- | --- |
| `INFO` (default) | General feedback, such as confirming that changes were saved. | Neutral grey (`#434755`) |
| `ERROR` | Report a failure, such as a save that could not complete. | Error red (`#EE2B2E`) |

## Show a Toast

The returned Material `Snackbar` supports callbacks and manual dismissal.

```kotlin
import id.co.edtslib.uikit.R as UIKitR
import id.co.edtslib.uikit.core.snackbar.CoreSnackbar
import id.co.edtslib.uikit.recipes.snackbar.klik.KlikToast
import id.co.edtslib.uikit.recipes.snackbar.poinku.PoinkuToast

KlikToast.show(host = binding.root, message = "Saved")

PoinkuToast.show(
    host = binding.root,
    message = "Could not save",
    variant = CoreSnackbar.Variant.ERROR,
    iconRes = UIKitR.drawable.ic_placeholder_medium_24,
    actionText = "Retry",
    anchor = binding.saveButton
) {
    retrySave()
}
```

Treat icons as decorative and put the status in the message. Android elevation renders each product’s shadow.

## Configuration

`CoreSnackbar.show()`, `KlikToast.show()`, and `PoinkuToast.show()` accept the same parameters.

| Parameter | Default | Behavior |
| --- | --- | --- |
| `host: View` | Required | An attached view used to find the Snackbar's parent. |
| `message: CharSequence` | Required | Message displayed in the Snackbar. |
| `variant: Variant` | `Variant.INFO` | Selects the Info or Error recipe. |
| `iconRes: Int?` | `null` | Drawable resource for the leading icon. Omit it to hide the icon. |
| `actionText: CharSequence?` | `null` | Action label. Omit it or pass an empty label to hide the action. |
| `anchor: View?` | `null` | Positions the Snackbar above this view. With no anchor, Material uses its default bottom placement. |
| `duration: Int` | `Snackbar.LENGTH_LONG` | A Material duration constant or a positive duration in milliseconds. |
| `animationMode: Int` | `Snackbar.ANIMATION_MODE_FADE` | Use fade or pass `Snackbar.ANIMATION_MODE_SLIDE` for a slide transition. |
| `onAction: ((View) -> Unit)?` | `null` | Called with the action view when tapped. Material then dismisses the Snackbar. |

## Migrate an existing caller

Existing `View.snack(...)` and `View.alertSnack(...)` calls still work. When adopting a product recipe, map `AlertType.DEFAULT` to `Variant.INFO` and `AlertType.ERROR` to `Variant.ERROR`. Map `startIconRes` to `iconRes`, `action` to `onAction`, and pass the anchor view directly when anchoring is enabled. Legacy appearance overrides and styled-message settings remain available on the original helpers.

[Klik Toast](https://www.figma.com/design/Tpv7igrzhayWSY0z15u2RB/Klik-Design-System-2.0?node-id=8-358) · [Poinku Toast](https://www.figma.com/design/7e2kAWACnJlfcG0VONJJFP/Poinku-UI-Library---Mobile?node-id=5-20)
