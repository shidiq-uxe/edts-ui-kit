# StepperButton

`StepperButton` retains its existing tap and optional count-editing APIs. Long-press drag
repetition is opt-in and is currently enabled by the TMH redemption prototype.

```kotlin
stepper.apply {
    setAllowTextEditing(false)
    isAlwaysExpanded = true
    isDragRepeatEnabled = true
    isStepperHapticFeedbackEnabled = true
    isDragFeedbackEnabled = true
    dragRepeatConfig = StepperDragRepeatConfig(
        deadZoneDp = 12f,
        maximumDistanceDp = 108f,
        minimumChangesPerSecond = 2f,
        maximumChangesPerSecond = 8f,
    )
    setMinValue(0)
    setMaxValue(availableQuantity)
    setCount(currentQuantity, animate = false)
    setOnCountChangeListener { quantity -> /* Update the pending selection. */ }
}
```

Import `StepperButton` and `StepperDragRepeatConfig` from `id.co.edtslib.uikit.stepper`.
`isDragRepeatEnabled` and `isAlwaysExpanded` default to `false`; enabling the gesture does
not itself pin the control open. `isAlwaysExpanded` keeps both controls and the count visible
at zero and cancels any pending legacy expand/collapse transition.

## Touch-target modes

`buttonTouchTargetMode` defaults to `StepperButtonTouchTargetMode.COMPACT`. It enlarges
button hit regions toward 32×32dp inside the existing host, without adding measured space.
The visible buttons remain 24×24dp and the expanded pill remains 90×32dp. For the standard
noneditable pill, both 32dp targets fit. For editable pills, the existing 34dp editor is
protected; this leaves approximately 28×32dp for each button in the unchanged 90dp host.
Rounding can vary by one pixel with density.

Opt in to 48dp targets using a wrap-content host:

```xml
<id.co.edtslib.uikit.stepper.StepperButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:buttonTouchTargetMode="expanded" />
```

In Kotlin, set `buttonTouchTargetMode = StepperButtonTouchTargetMode.EXPANDED`.
This mode reserves an approximately 106×48dp host for the standard noneditable expanded
pill, or 130×48dp with editing enabled. The visible pill stays centered at 90×32dp.
The collapsed control reserves 48×48dp. Editable button targets extend outward so they
do not overlap the editor. Callers must allow this extra host space in their constraints.
Parent EXACTLY/AT_MOST constraints win: smaller hosts clip the requested targets rather
than drawing or receiving touches outside their bounds. No parent touch delegate is
installed or replaced, and no neighboring control's touch area is claimed.

A touch starting inside the editor retains native editing, cursor/selection, and keyboard
behavior. A touch starting on a button uses the same tap/long-press/drag route throughout
the gesture, including points outside its visible icon. Changing modes cancels drag repeat.
Compact targets improve the previous 24dp area but do not meet the recommended 48dp size.
See [Android touch-target guidance](https://support.google.com/accessibility/android/answer/7101858?hl=en).

## Haptic and visual feedback

Both `isStepperHapticFeedbackEnabled` and `isDragFeedbackEnabled` default to `false` for
existing callers. TMH enables both. These are also XML boolean attributes.

| Trigger/state | Feedback |
|---|---|
| Long-press activation | One platform `GESTURE_START` pulse on API 30+, `LONG_PRESS` on older versions; quantity stays unchanged |
| Accepted tap, drag-repeat step, or committed count edit | One `SEGMENT_FREQUENT_TICK` on API 34+, `CLOCK_TICK` on older versions |
| Programmatic `setCount`, dead zone, ignored minus-up gesture, blocked limit, release, or cancellation | No additional haptic |
| Active drag | Blue pill outline and a non-touchable direction/speed hint above the control |
| Paused | One static chevron in the last active direction, beside `Tambah` / `Kurangi`; initially up for plus and down for minus |
| Increasing/decreasing | `Tambah` / `Kurangi`; upward/downward chevrons with a highlight travelling in that direction |
| Blocked direction | `Batas tercapai`; highlight stops immediately |
| End/cancel | Restore the original outline; fade the hint out |

The hint fades in/out over 150ms with deceleration, without changing row measurements.
Its width reserves the longest translated label at the current font size, avoiding jumps
when state changes. It cannot consume touches or take focus. System-disabled animations
snap directly to the final state; window hiding and detachment dismiss it immediately.
The existing distance-based repeat-speed levels determine the chevron count and highlight
cycle. Finger velocity is not used. These are initial tuning values, not repeat intervals:

| Existing speed level | Chevrons | Highlight cycle |
|---|---|---|
| Slow | 1 | 900ms |
| Medium | 2 | 600ms |
| Fast | 3 | 350ms |

The fixed 24×24dp indicator reserves all three chevrons, keeping the bubble size stable
between levels. A speed change preserves the highlight phase; reversal restarts travel in
the new direction. Neutral/blocked states stop the loop and show a static cue. Release or
cancellation stops the loop before the bubble fades out, and indicator detachment also
stops it. With system animations disabled, active direction and speed remain visible as
one, two, or three fully opaque static chevrons. Native `ValueAnimator` drives the highlight;
no animation dependency or public API is added.

Haptics use `View.performHapticFeedback`, honor the view/system settings, and require no
new vibration permission. Hardware can soften or omit effects, especially frequent ticks;
physical-device validation is needed to judge sensation. See
[Android haptic feedback](https://developer.android.com/develop/ui/views/haptics/haptic-feedback).

## Gesture behavior

An enabled plus or minus button starts tracking on touch-down. Moving beyond Android touch
slop before the platform long-press timeout cancels activation, allowing normal parent
scrolling. A stationary long press activates without changing the count.

The activation finger position becomes the vertical origin. Offset above the origin permits
increases for plus; offset below permits decreases for either button. Minus ignores offsets
above the origin. Returning within the dead zone pauses. Direction is based on position
relative to that origin, not the direction of the most recent movement.

The default repeat interval is derived from the offset magnitude:

| Offset magnitude | Behavior |
|---|---|
| 0–12dp | Paused |
| Just beyond 12dp | Approximately 2 changes/second |
| 60dp | 5 changes/second, 200ms interval |
| 108dp or farther | 8 changes/second, 125ms interval |

The rate increases linearly between the configured distances. The first change waits for
the repeat interval. Movement updates the next deadline without continually restarting the
timer, so repetition also works while the finger keeps moving. No quantity changes are
queued at a limit or while paused.

After activation the component consumes the gesture and blocks ancestor interception. The
finger may leave the button bounds while remaining in the app's touchable window. Release
ends repetition and does not produce an additional button click. Cancellation, a second
pointer, disabled component state, window hiding, and detachment stop tracking/callbacks.
Already accepted changes remain pending in the caller's model.

## Limits and lifecycle

Use `setMinValue`, `setMaxValue`, `setPlusButtonEnabled`, and `setMinusButtonEnabled` to
define currently permitted changes. An active plus gesture can decrease after its plus
button becomes disabled at the cap, provided minus remains enabled. A disabled button
cannot start a new gesture. Disable the whole component when the item becomes unavailable.

For a shared fair allowance, set each product's maximum to:

```kotlin
productQuantity + fairMaximum - totalPendingQuantity
```

Update bounds alongside model changes. Ordinary same-item rebinding can call `setCount`
without ending the gesture. Call `cancelDragRepeat()` when rebinding the view to a different
item, dismissing its owner, or otherwise invalidating the operation. Changing the gesture
configuration or turning the mode off also cancels tracking.

The component removes repeat callbacks on detach and can start a new gesture after
reattachment. Parent RecyclerView change animations that replace the touched holder must
be disabled when live quantity updates require uninterrupted dragging. TMH disables those
animations and supplies explicit border/corner selection feedback instead.

These APIs do not save a cart or own its eligibility rules. TalkBack, keyboard, and ordinary
button clicks retain the existing single-step behavior. Gesture cadence is independent of
the system animator-duration scale.
