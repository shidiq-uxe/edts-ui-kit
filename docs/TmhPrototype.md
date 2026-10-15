# Tebus Murah dan Hadiah prototype

The sample app's TMH guideline covers campaign navigation, redemption trays, local cart
selection, unsaved-change confirmation, and scenario switching. Long-press the toolbar title
to select a scenario. Hadiah Produk is displayed but does not navigate.

[Default Figma reference](https://www.figma.com/design/Wd140rqS27zbhDNDSpy9PB/?node-id=63815-136703)

## Default campaign fixtures

| Order | Campaign | State/action |
|---|---|---|
| 1 | Toples Kaca Indomaret Fair | 1/2, Ubah |
| 2 | Luminarc Soup Idaman Fair | 2/2, Ubah; one of each product |
| 3 | Paragon Fair | 0/2, Pilih; Kahf, Wardah, Emina |
| 4 | Softex Fair | Requirement unmet, Tambah |
| 5 | Toilet Tissue Fair | Stock unavailable, S&K |
| 6 | Vanish Fair | Global quota exhausted, S&K |
| 7 | 5 Days Fair | Personal limit exhausted, S&K |
| 8 | Chupa Chups Fair | First transaction only, S&K |

The demo cart contains 11 other items plus saved promotional quantities, initially totaling
14. This is sample data, not a cart-service integration. Individual state scenarios retain
three representative campaigns, including a restocking example.

## Interaction behavior

The fair strip uses Material `TabLayout` in scrollable mode with the shared rounded indicator.
The first fair tab aligns to the start, the final tab aligns to the end, and intermediate
tabs center where the scroll range permits. Tapping scrolls the associated card to the
content top where space permits, and the final card stops at the natural list bottom.
The final card has 12dp clearance plus the system safe area once, without extra space to force
top alignment. Manual scrolling selects the first visible campaign; at the bottom, the final
tab takes priority even when an earlier card remains first visible. This also applies when a
near-end tab's scroll is clamped at the bottom. A new tap supersedes the
previous scroll; manual dragging takes over vertical navigation.

A selected product has a blue outline and a 32dp triangular blue corner with a white check.
At 2/2, plus is disabled while minus remains available. An unavailable product cannot be
incremented. Saving a changed selection can remove all previously saved promotional items;
an unchanged fresh 0/2 selection cannot be saved.

Long-press an enabled stepper button to activate vertical repetition without changing the
quantity. From plus, an offset above the activation point repeats increases; below repeats
decreases. From minus, only offsets below the activation point repeat decreases. The 12dp
dead zone pauses; farther offsets accelerate linearly from 2 to 8 changes/second, capped at
108dp. Returning toward the origin slows/pauses, and crossing it reverses plus. Release
does not add a click. The active gesture captures input from the list and sheet; movements
before activation retain normal scrolling. See [StepperButton](StepperButton.md) for the API.

TMH enables a light activation haptic and a soft tick for each accepted tap/drag count change.
During drag, a blue outline and a direction/speed hint acknowledge the acquired gesture.
The hint shows paused guidance, increase/decrease with three speed levels, or `Batas tercapai`
at a blocked limit. It fades in/out over 150ms, takes no touch/focus, and adds no row space.
Blocked changes, pause, release, cancellation, and programmatic rebinding stay silent.
Compact touch targets expand toward 32dp while retaining the current 90×32dp stepper footprint.
The shared component also supports an explicit larger 48dp target mode for callers that allow
the additional host space; this mode is not enabled in the TMH row.

The redemption footer is 40dp high and follows the original per-product selection:

| Original selection | Current selection | Footer |
|---|---|---|
| Fresh 0/2 | Still 0/2 | Disabled gray **+ Keranjang** |
| Fresh 0/2 | One or more products | Enabled blue **+ Keranjang** |
| Saved selection | Identical product quantities | Disabled gray **Perbarui Keranjang** |
| Saved selection | Different nonzero quantities | Enabled blue **Perbarui Keranjang** |
| Saved selection | All quantities zero | Enabled red **Hapus** |

Changing products without changing the total is a valid update. Returning to the original
product quantities disables the action. Committing Add, Update, or Delete dismisses the tray,
then shows page loading, restores the campaign and its scroll offset, and displays success.
Ordinary dismissal or discarding unsaved edits does not reload.

Loading appears on initial entry, pull-to-refresh, and those three committed actions. The
toolbar and promotion tabs stay visible; rounded skeleton tabs and cards follow the
[loading frame](https://www.figma.com/design/Wd140rqS27zbhDNDSpy9PB/?node-id=64096-332739).
Cards include title, banner, horizontal product tiles, and alert placeholders.

Sheets use normal bounded framework measurement and are capped at 90% of the current window height. Long content
scrolls above the fixed footer. Footer padding includes dialog-window system-bar and cutout
insets once; sheets without a footer protect their last content instead. X, Back, scrim, and
swipe-down all ask for confirmation only when quantities differ from the original selection.
Each dialog window dims its background with 75% black (Figma RGBA `#000000BF`, Android ARGB
`#BF000000`). Stacked terms and unsaved-change sheets each apply their own dimming.

## Typography implementation and audit

TMH text appearances explicitly use the existing bundled Inter font files at weights 400,
500, 600, and 700, removing dependency on Google Play Services font delivery for this screen.
Shared library text appearances are preserved.

| Text role | Font/size/line height |
|---|---|
| Toolbar and tray title | Inter Semibold, 16sp / 18dp |
| Active promotion tab; fair heading | Inter Semibold, 14sp / 16dp |
| Inactive promotion tab; tray pack label | Inter Medium, 12sp / 16dp |
| Fair tabs; preview price | Inter Semibold, 12sp / 16dp |
| Preview name and alert body | Inter Regular, 12sp / 16dp |
| Discount badge | Inter Bold, 10sp / 14dp |
| Original price | Inter Regular, 10sp / 12dp |
| Tray name / price | Inter Regular / Semibold, 14sp / 16dp |
| Description | Inter Regular, 14sp / 20dp |
| Stepper count | Inter Semibold, 14sp / 16dp |
| Tray footer button | Inter Semibold, 14sp / 24dp |

Alert quantities and eligibility amounts use the shared HTML-aware `buildHighlightedMessage`
API with true Semibold spans and the darker emphasized color. `Total dapat` uses a regular prefix and semibold quantity/unit. Font padding is disabled
in TMH labels. The inactive promotion-tab bottom strip is intentionally removed following
project feedback, although it exists in the reference.

## Motion report

Figma returned no keyframe tracks for the default frame. The following values are nominal
source-defined timings from the app and pinned Android dependencies, not frame-by-frame
measurements. Android animation settings and accessibility behavior may change observed motion.

| Trigger | Motion | Duration |
|---|---|---|
| Fair tab tap | Campaign scroll, linear travel then deceleration; top aligned where possible, clamped at the natural bottom | Distance-dependent; LinearSmoothScroller base speed 25ms/inch |
| Fair becomes selected | Material TabLayout horizontal scrolling, bounded to start/end at the endpoints | 300ms when already laid out; immediate initial positioning |
| Product-list swipe | Native drag/fling | Follows finger; fling depends on release velocity |
| Active-tab label/underline | Label state changes immediately; Material underline translates between tabs | 300ms indicator animation; immediate initial positioning |
| Sheet opens | Material 3 upward translation from 20% parent offset plus fade, emphasized easing | 400ms |
| X, Back, scrim, or local save closes sheet | Material 3 downward translation and fade in parallel | Translation 350ms; fade 300ms |
| Sheet swipe release or dirty-dismiss restoration | Native drag settling | Depends on distance/velocity |
| Unsaved confirmation opens | Same native sheet entrance | 400ms |
| Discard unsaved selection | Confirmation and redemption sheets close; platform window sequencing controls their overlap | Each uses translation 350ms / fade 300ms |
| Product quantity `0 → nonzero` | Immediate quantity update; border turns blue, triangle fades/slides diagonally into its corner | Border 250ms; corner 250ms ease-out |
| Product quantity `nonzero → 0` | Immediate quantity update; border turns gray, triangle fades/slides diagonally out and becomes hidden | Border 250ms; corner 200ms ease-in |
| Product quantity changes within nonzero | Count, total, limit, and footer update; corner remains selected | Immediate; no corner replay |
| Stepper long press | Capture gesture at the platform long-press timeout; stationary position pauses | Platform-configured timeout; no quantity change on activation |
| Active stepper drag | Repeat quantity changes according to signed distance from activation point; show direction/speed hint and blue outline | Approximately 500ms down to 125ms between changes; hint fades in/out over 150ms; no inertia or extra release click |
| Add, Update, or Delete | Sheet dismissal publishes the committed selection; skeleton loading then restored listing/badge and success snackbar | 1200ms simulated loading; native sheet exit and snackbar timings apply |
| Shimmer loading | Repeated left-to-right highlight | 1000ms/cycle; simulated loading delay 1200ms |
| Loaded content replaces skeleton | Immediate adapter replacement | No crossfade |
| Snackbar enters/exits | Fade plus entrance scale 0.8 to 1; Material 3 theme durations | 500ms in; 250ms out |
| Error snackbar | Same entrance; remains until dismissed or replaced | Indefinite visible duration |
| Hadiah Produk tap | Navigation prevented | No transition |

The shared promotion-tab indicator's optional 250ms animation is not enabled here. Off-screen
fair tabs use Material tab selection and scrolling rather than a separate RecyclerView adapter.
Tray product RecyclerView change animations are disabled to preserve the touched holder.
Card transitions retarget from their current visible values when interrupted. New-product
binding and detach apply the final appearance immediately; disabled system animations also
apply the final state synchronously. Quantities update independently of visual animation.

Sources: the TMH Activity and sheet classes; RecyclerView 1.3.0 and Shimmer 0.5.0 sources;
[Material TabLayout 1.12.0 implementation](https://github.com/material-components/material-components-android/blob/1.12.0/lib/java/com/google/android/material/tabs/TabLayout.java);
Material 1.12.0 animation resources and [snackbar implementation](https://github.com/material-components/material-components-android/blob/1.12.0/lib/java/com/google/android/material/snackbar/BaseTransientBottomBar.java).

## Prototype limitations

Eligibility amounts remain marked as inconsistent sample content (Rp40.000, Rp50.000,
and Rp100.000 across designs). Cross-device cart changes and cart-save failures remain TODOs.
Device-specific Vivo behavior needs confirmation on the physical device after installation;
emulator verification cannot substitute for that check.
Consolidating the feature-local TMH TextAppearance XML and Kotlin helpers into shared
typography remains deferred TODO at the user's request.
