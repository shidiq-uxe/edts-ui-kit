# PDP discount animation

Use `LayoutTransition` on the information-section root to animate the discount badge and harga coret. Constrain the selector below the discount row so it moves with the layout.

## Animations and durations

| Animation / scenario       | Behavior                                                               | Duration                                    |
|----------------------------|------------------------------------------------------------------------|---------------------------------------------|
| Collapse space             | Discounts become `GONE`; selector moves immediately.                   | **0 ms**                                    |
| Keep space                 | Discounts become `INVISIBLE` during spotlight; space stays reserved.   | **0 ms**; no layout movement                |
| Animate space              | Discounts fade out/in while space collapses/expands.                   | **300 ms**, with **0 ms** start delays      |
| Spotlight follows selector | Cutout, card, and default pointer follow the target's rendered bounds. | Follows layout frames, no separate duration |

When the selected variant has a discount, both views are `VISIBLE` in every scenario. Hidden discounts use `GONE` once the spotlight ends.

## Mirror the animation

On the main thread, configure the information-section container once before changing visibility in **Animate space**:

```kotlin
import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup

val container = binding.root as ViewGroup
container.layoutTransition = LayoutTransition().apply {
    setDuration(300L)
    setStartDelay(LayoutTransition.APPEARING, 0L)
    setStartDelay(LayoutTransition.DISAPPEARING, 0L)
    setStartDelay(LayoutTransition.CHANGE_APPEARING, 0L)
    setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0L)
}
```

For the other scenarios, set `container.layoutTransition = null`. This assumes your screen owns the container's transition.

Update prices first, then apply visibility using the selected variant's discount state:

```kotlin
val visibility = when {
    hasDiscount -> View.VISIBLE
    keepSpace && spotlightVisible -> View.INVISIBLE
    else -> View.GONE
}
binding.discountBadge.visibility = visibility
binding.tvProductPriceCutOff.visibility = visibility
```

`keepSpace` means the Keep space scenario; `spotlightVisible` comes from the active overlay. Fading and layout movement overlap; no separate selector animator is needed.

## Keep the spotlight aligned

Pass the selector View as the target:

```kotlin
CoachMarkData(
    target = binding.pdpSelectionGroup,
    title = "Tambah Produk Lebih Cepat",
    description = "Pilih Karton atau Satuan."
)
```
