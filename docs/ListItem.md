# ListItem

`ListItem` manages the behavior of a single list item. It turns input on a row into typed events and keeps the row's views in sync with its state, without owning data or layout.

## Architecture

The system is built around four layers:

- **`ListItem`** — binds an item and state to a row, dispatches events and drives the renderer.
- **`ListItemEvent`** — one event stream: click, long click, custom action, state change.
- **`ListItemGestureBinding`** — pluggable input (click, long click, custom).
- **`ActionKey`** — typed token that names what a gesture means.

| Class / Object | Responsibility |
| --- | --- |
| `ListItem` | Entry point: bind, update, dispatch events, run guards and the renderer |
| `ListItemEvent` | Sealed event type: `Click`, `LongClick`, `Custom`, `StateChanged` |
| `ActionKey` | Identity token for an action; carries the payload type |
| `ListItemGestureBinding` | Attaches one kind of input to a view and returns a function that detaches it |
| `StandardGestures` | Built-in `click` and `longClick` bindings |
| `ListItemListener` / `ListItemGuard` / `ListItemRenderer` | Receives events / vetoes events given the state / draws `(item, state)` |

## Core Classes

### List Item

```kotlin
class ListItem<T : Any, S : Any>(root: View, defaultState: S)
```

| Method | Description |
| --- | --- |
| `bind(item, initial)` / `unbind()` | Binds an item and renders it (no event) / renders the default state and clears the item |
| `release()` | Unbinds and drops all gestures, listeners, guards and handlers |
| `refresh()` | Re-renders the current item and state |
| `update(transform): Boolean` | Changes state and emits `StateChanged`; `false` if no item, same state or vetoed |
| `setRenderer(r)` | Sets the renderer |
| `addListener` / `removeListener` | Subscribes to events |
| `addGuard` / `removeGuard` | Adds or removes a veto |
| `enableCardClicks` / `disableCardClicks` | Row-level `Click` and `LongClick` events |
| `setClickDebounce(ms)` | Per-view click debounce, 300 ms by default; `0` disables |
| `bindAction` / `bindActions` / `unbindAction` | Binds a gesture on a view to a key, or removes it |
| `onAction(key)` / `clearActionHandlers` | Handler for one key |
| `triggerAction(key, payload, view)` | Emits a `Custom` without a gesture |

### List Item Event

| Event | Fires when |
| --- | --- |
| `Click` / `LongClick` | The card root is tapped or long pressed (`enableCardClicks`) |
| `Custom` | A bound gesture fires, or `triggerAction` is called |
| `StateChanged` | `update { }` changes the state and no guard vetoes it |

> `ActionKey` is matched by reference. Create each key once and share it.

## Usage

### Basic Usage

```kotlin
object ProductActions {
    val Image = ActionKey<Unit>("image")
    val QuantityLimit = ActionKey<Int>("quantity_limit")   // payload = max allowed
}
data class ProductCardState(val quantity: Int = 0, val loading: Boolean = false)

// ViewHolder: wire once
val card = ListItem<Product, ProductCardState>(binding.root, ProductCardState())
    .enableCardClicks()
    .bindAction(binding.ivImage, ProductActions.Image)
    .setRenderer { _, item, state -> render(item, state) }
    .addListener(listener)

// Adapter
onBindViewHolder:  holder.card.bind(row.product, row.state)
onViewRecycled:    holder.card.unbind()

// Screen
val listener = ListItemListener<Product, ProductCardState> { e ->
    when (e) {
        is ListItemEvent.Click -> openDetail(e.item)
        is ListItemEvent.Custom -> if (e.key = = ProductActions.Image) openGallery(e.item)
        is ListItemEvent.StateChanged -> vm.setQuantity(e.item.id, e.newState.quantity)
        is ListItemEvent.LongClick -> Unit
    }
}
```

### Actions and State

```kotlin
card.bindAction(view, Rate) { 5 }                              // with a payload
card.bindAction(view, ImageLong, StandardGestures.longClick)   // another gesture
card.onAction(Rate) { item, stars, view -> /* ... */ }         // handler for one key
card.triggerAction(QuantityLimit, max, view)                   // no gesture needed
card.update { it.copy(quantity = it.quantity + 1) }            // false if no item, same state or vetoed
card.addGuard { event, state -> !state.loading }               // false drops the event
```

### Custom Gestures

`StandardGestures` covers click and long click. Any other input (double tap, swipe, a touch on a drag handle) becomes a gesture by implementing `ListItemGestureBinding`. The binding only detects the input; the key passed to `bindAction` decides what it means for the item.

How a custom gesture runs:

1. `attach(view, onGesture)` installs the listener on the view.
2. When the input happens, the binding calls `onGesture(ListItemGesture.Custom(name), view)`.
3. `attach` returns a function that undoes everything it installed. The controller calls it on `unbindAction`, when the same view and binding are bound again, and on `release()`.
4. The controller wraps the result in a `ListItemEvent.Custom` that carries the key from `bindAction`, then runs guards, listeners and handlers as for any other action.

```kotlin
val Like = ActionKey<Unit>("like")

val doubleTap = ListItemGestureBinding { view, onGesture ->
    val detector = GestureDetector(view.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onGesture(ListItemGesture.Custom("double_tap"), view)
            return true
        }
    })
    view.setOnTouchListener { _, event -> detector.onTouchEvent(event) }
    val detach: () -> Unit = { view.setOnTouchListener(null) }
    detach
}

card.bindAction(binding.ivImage, Like, doubleTap)
card.onAction(Like) { item, _, _ -> vm.like(item.id) }
```
