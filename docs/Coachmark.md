# CoachMarkOverlay

A customizable onboarding overlay that highlights UI elements with an animated spotlight cutout and a positioned tooltip card.

---

## Table of Contents

- [Basic Usage](#basic-usage)
- [Coach Mark Items](#coach-mark-items)
- [Spotlight Target](#spotlight-target)
- [Spotlight Appearance](#spotlight-appearance)
- [Vertical Positioning](#vertical-positioning)
- [Custom Layout](#custom-layout)
- [Delegate](#delegate)
- [Builder Reference](#builder-reference)
- [Direct API Reference](#direct-api-reference)

---

## Basic Usage

The recommended way to create a `CoachMarkOverlay` is through the `Builder`.

```kotlin
CoachMarkOverlay.Builder(this) // FragmentActivity
    .setCoachMarkItems(
        CoachMarkData(title = "Search", description = "Tap here to search.", target = searchView),
        CoachMarkData(title = "Profile", description = "Manage your account here.", target = profileIcon)
    )
    .build()
```

By default, the overlay attaches itself to the activity's decor view. No manual container setup needed unless you want to scope it to a specific `ViewGroup`.

---

## Coach Mark Items

`CoachMarkData` is the unit of content per step.

```kotlin
data class CoachMarkData(
    val title: String,
    val description: String,
    val target: View
)
```

Pass items as a list or vararg:

```kotlin
// Vararg
.setCoachMarkItems(item1, item2, item3)

// List
.setCoachMarkItems(listOf(item1, item2, item3))
```

### Step Progress Divider

The step counter displays as `1/3` by default. Customize the divider:

```kotlin
.setStepProgressDivider(" of ") // renders as "1 of 3"
```

### Button Labels

```kotlin
.setCoachmarkNextText("Next")
.setCoachmarkSkipText("Skip")
```

> The last step automatically replaces the next button label with the close text (`"Tutup"` by default). Configure it via `closeDefaultText` on the overlay directly if needed.

### Skip Button Visibility

The skip button is visible on all steps except the last by default.

```kotlin
.setSkipButtonVisibleByDefault(false) // hide skip button entirely
```

---

## Spotlight Target

### From a View

The standard case. The overlay waits for the view to be laid out before computing its position.

```kotlin
CoachMarkOverlay.Builder(this)
    .setCoachMarkItems(
        CoachMarkData("Cart", "Review your items.", target = cartButton)
    )
    .build()
```

The spotlight shape defaults to `ROUNDED_RECTANGLE`. Override per item via `setTargetView()` directly on the overlay if needed:

```kotlin
overlay.setTargetView(myView, shape = CoachMarkOverlay.SpotlightShape.CIRCLE)
```

### From a Rect (Non-View Targets)

Use `setTargetRect` when the target has no `View` backing — e.g. a canvas-drawn element, a chart bar, or a map pin.

> **Important:** Coordinates must be in window space. The caller is responsible for ensuring the rect is valid before calling this — no layout deferral occurs.

```kotlin
// Example: highlighting a bar in a custom chart view
val chartLocation = IntArray(2)
chartView.getLocationInWindow(chartLocation)

val barRect = RectF(
    chartLocation[0] + barLeft,
    chartLocation[1] + barTop,
    chartLocation[0] + barRight,
    chartLocation[1] + barBottom
)

overlay.setTargetRect(barRect, CoachMarkOverlay.SpotlightShape.ROUNDED_RECTANGLE)
```

```kotlin
// Example: highlighting a RecyclerView item
recyclerView.doOnPreDraw {
    val itemView = layoutManager.findViewByPosition(targetPosition) ?: return@doOnPreDraw
    val loc = IntArray(2)
    itemView.getLocationInWindow(loc)

    val rect = RectF(
        loc[0].toFloat(),
        loc[1].toFloat(),
        (loc[0] + itemView.width).toFloat(),
        (loc[1] + itemView.height).toFloat()
    )
    overlay.setTargetRect(rect)
}
```

```kotlin
// Example: highlighting a map pin
val screenPoint = mapView.projection.toScreenLocation(latLng)
val rect = RectF(
    screenPoint.x - PIN_RADIUS,
    screenPoint.y - PIN_RADIUS,
    screenPoint.x + PIN_RADIUS,
    screenPoint.y + PIN_RADIUS
)
overlay.setTargetRect(rect, CoachMarkOverlay.SpotlightShape.CIRCLE)
```

---

## Spotlight Appearance

### Padding (Gap from View)

Controls the visual gap between the target view bounds and the edge of the spotlight cutout. Does not affect positioning calculations.

```kotlin
.setSpotlightPadding(8f) // dp, default 4f
```

### Corner Radius

Only applies to `ROUNDED_RECTANGLE` shape.

```kotlin
.setSpotlightCornerRadius(16f) // dp, default 24f
```

### Shape

`ROUNDED_RECTANGLE` is the default. Switch to `CIRCLE` for icon or avatar targets.

```kotlin
overlay.setTargetView(avatarView, shape = CoachMarkOverlay.SpotlightShape.CIRCLE)
```

### Coachmark Card Width

Expressed as a fraction of screen width.

```kotlin
.setCoachmarkWidthPercent(0.75f) // default 0.84f
```

### Text Appearance

```kotlin
.setCoachmarkTitleTextAppearance(R.style.MyTitleStyle)
.setCoachmarkDescriptionTextAppearance(R.style.MyDescriptionStyle)
```

---

## Vertical Positioning

By default the overlay picks above or below the target based on available space and whether the coachmark card actually fits. Override this with `setVerticalPosition`.

### Auto (Default)

```kotlin
.setVerticalPosition(CoachMarkOverlay.CoachMarkVerticalPosition.Auto)
```

Checks if the card fits above or below. If both fit, prefers below. If neither fits, clamps to the roomier side.

### Force Above

```kotlin
.setVerticalPosition(CoachMarkOverlay.CoachMarkVerticalPosition.Above)
```

Useful for targets near the bottom of the screen (e.g. bottom navigation items).

### Force Below

```kotlin
.setVerticalPosition(CoachMarkOverlay.CoachMarkVerticalPosition.Below)
```

Useful for targets in the toolbar or status bar area.

### Custom Gap

All positions accept an optional `gapDp` parameter:

```kotlin
.setVerticalPosition(CoachMarkOverlay.CoachMarkVerticalPosition.Below, gapDp = 16f)
```

### Absolute Y

Pin the coachmark card to a fixed Y coordinate in window space. Intended for use with `setTargetRect` on non-View targets.

```kotlin
overlay.setVerticalPosition(
    CoachMarkOverlay.CoachMarkVerticalPosition.Absolute(screenPoint.y + PIN_RADIUS + 12f)
)
```

---

## Custom Layout

Replace the default card entirely with your own layout. When a custom layout is set, the default next/skip buttons are removed — you wire navigation manually.

### From a View instance

```kotlin
val myCard = MyCoachMarkCardView(context)

CoachMarkOverlay.Builder(this)
    .setCoachMarkItems(items)
    .setCustomLayout(myCard) { overlay ->
        myCard.onNextClicked = { overlay.showNextCoachMark() }
        myCard.onSkipClicked = { overlay.dismiss {} }
    }
    .build()
```

### From a layout resource

```kotlin
CoachMarkOverlay.Builder(this)
    .setCoachMarkItems(items)
    .setCustomLayout(R.layout.my_coachmark_card) { view, overlay ->
        view.findViewById<Button>(R.id.btnNext).setOnClickListener {
            overlay.showNextCoachMark()
        }
        view.findViewById<Button>(R.id.btnSkip).setOnClickListener {
            overlay.dismiss {}
        }
    }
    .build()
```

> **Note:** With a custom layout, `updateCoachMarkContent()` is a no-op — step title, description, and counter are your responsibility to update. Use `getCurrentCoachmarkIndex()` and `onNextClickClickListener` in the delegate to sync your UI.

---

## Delegate

React to user interactions and overlay lifecycle events.

```kotlin
class MyFragment : Fragment(), CoachmarkDelegate {

    override fun onNextClickClickListener(index: Int) {
        // Called when the next button is tapped, before transitioning
    }

    override fun onSkipClickListener(index: Int) {
        // Called when skip is tapped, before dismissing
    }

    override fun onDismissListener() {
        // Called after the overlay finishes its exit animation
    }
}
```

```kotlin
CoachMarkOverlay.Builder(this)
    .setCoachMarkDelegate(this)
    .setCoachMarkItems(items)
    .build()
```

### Back Press

By default the back button does not dismiss the overlay. Opt in:

```kotlin
.setDismissibleOnBack(true)
```

---

## Builder Reference

| Method                                       | Type                           | Default         | Description                        |
|----------------------------------------------|--------------------------------|-----------------|------------------------------------|
| `setCoachMarkItems(...)`                     | `List<CoachMarkData>` / vararg | —               | Steps to display                   |
| `setCoachMarkDelegate(...)`                  | `CoachmarkDelegate`            | `null`          | Event callbacks                    |
| `setContainer(...)`                          | `ViewGroup`                    | Decor view      | Parent to attach overlay to        |
| `setDismissibleOnBack(...)`                  | `Boolean`                      | `false`         | Allow back press to dismiss        |
| `setVerticalPosition(...)`                   | `CoachMarkVerticalPosition`    | `Auto`          | Card placement strategy            |
| `setSpotlightPadding(...)`                   | `Float` (dp)                   | `4f`            | Visual gap around target           |
| `setSpotlightCornerRadius(...)`              | `Float` (dp)                   | `24f`           | Corner radius of spotlight         |
| `setCoachmarkWidthPercent(...)`              | `Float`                        | `0.84f`         | Card width as fraction of screen   |
| `setCoachmarkTitleTextAppearance(...)`       | `@StyleRes Int`                | Rubik H3 Medium | Title text style                   |
| `setCoachmarkDescriptionTextAppearance(...)` | `@StyleRes Int`                | Rubik P2 Light  | Description text style             |
| `setCoachmarkNextText(...)`                  | `String`                       | `"Berikutnya"`  | Next button label                  |
| `setCoachmarkSkipText(...)`                  | `String`                       | `"Tutup"`       | Skip button label                  |
| `setSkipButtonVisibleByDefault(...)`         | `Boolean`                      | `true`          | Show/hide skip button              |
| `setStepProgressDivider(...)`                | `String`                       | `"/"`           | Divider in step counter            |
| `setCustomLayout(view, handler)`             | `View`                         | —               | Replace default card with a View   |
| `setCustomLayout(layoutRes, handler)`        | `@LayoutRes Int`               | —               | Replace default card with a layout |

---

## Direct API Reference

For cases where you need to drive the overlay programmatically after creation.

```kotlin
overlay.showNextCoachMark()       // advance to next step or dismiss if last
overlay.dismiss { }               // animate out and hide the overlay
overlay.startSpotlightAnimation() // replay entrance animation
overlay.getCurrentCoachmarkIndex() // current zero-based step index

overlay.setTargetView(view)       // re-target to a new view
overlay.setTargetRect(rect)       // re-target to a raw rect
overlay.setVerticalPosition(CoachMarkOverlay.CoachMarkVerticalPosition.Above)
overlay.setSpotlightPadding(12f)
overlay.setSpotlightCornerRadius(8f)
```