# Snackbar and Toast recipes

Use Snackbar for brief in-app feedback. Klik and Poinku call their styled Snackbar a Toast. Each recipe shows an Info or Error message with an optional leading icon and a single optional action.

| Entry point | Message font | Action font |
| --- | --- | --- |
| `CoreSnackbar` | Inter Regular | Inter SemiBold |
| `KlikToast` | Inter Regular | Inter SemiBold |
| `PoinkuToast` | Rubik Regular | Rubik Medium |

## Show a Toast

Pass an attached view as the host. Material Snackbar positions the bar above `anchor` when supplied. The default duration is `Snackbar.LENGTH_LONG`. The returned Snackbar supports Material callbacks and manual dismissal.

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

Omit `iconRes` or `actionText` to hide that slot. Treat icons as decorative and put the status in the message. Material dismisses the Snackbar after the action runs. Android elevation renders each product’s shadow.

## Migrate an existing caller

Existing `View.snack(...)` and `View.alertSnack(...)` calls still work. When adopting a product recipe, map `AlertType.DEFAULT` to `Variant.INFO` and `AlertType.ERROR` to `Variant.ERROR`. Map `startIconRes` to `iconRes`, `action` to `onAction`, and pass the anchor view directly when anchoring is enabled. Legacy appearance overrides and styled-message settings remain available on the original helpers.

[Klik Toast](https://www.figma.com/design/Tpv7igrzhayWSY0z15u2RB/Klik-Design-System-2.0?node-id=8-358) · [Poinku Toast](https://www.figma.com/design/7e2kAWACnJlfcG0VONJJFP/Poinku-UI-Library---Mobile?node-id=5-20)
