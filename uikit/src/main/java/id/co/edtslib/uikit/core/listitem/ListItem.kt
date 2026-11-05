package id.co.edtslib.uikit.core.listitem

import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes

class ListItem<T : Any, S : Any>(
    private val root: View,
    private val defaultState: S
) {
    val boundItem: T? get() = item

    var state: S = defaultState
        private set

    private var item: T? = null
    private val listeners = mutableListOf<ListItemListener<T, S>>()
    private val guards = mutableListOf<ListItemGuard<T, S>>()
    private val handlers = mutableMapOf<ActionKey<*>, MutableList<(T, Any, View) -> Unit>>()
    private var renderer: ListItemRenderer<T, S>? = null
    private var clickDebounceMs = 300L

    private class Attachment(
        val view: View,
        val binding: ListItemGestureBinding,
        val detach: () -> Unit
    )

    private val attachments = mutableListOf<Attachment>()

    fun addListener(l: ListItemListener<T, S>) = apply { listeners += l }
    fun removeListener(l: ListItemListener<T, S>) = apply { listeners -= l }

    fun addGuard(g: ListItemGuard<T, S>) = apply { guards += g }
    fun removeGuard(g: ListItemGuard<T, S>) = apply { guards -= g }

    fun setRenderer(r: ListItemRenderer<T, S>?) = apply {
        renderer = r
        refresh()
    }

    fun setClickDebounce(ms: Long) = apply { clickDebounceMs = ms }

    @Suppress("UNCHECKED_CAST")
    fun <P : Any> onAction(
        key: ActionKey<P>,
        handler: (item: T, payload: P, view: View) -> Unit
    ): ListItem<T, S> = apply {
        val wrapped: (T, Any, View) -> Unit = { item, payload, view -> handler(item, payload as P, view) }
        handlers.getOrPut(key) { mutableListOf() }.add(wrapped)
    }

    fun clearActionHandlers(key: ActionKey<*>? = null) = apply {
        if (key == null) handlers.clear() else handlers.remove(key)
    }

    fun bindAction(
        view: View,
        key: ActionKey<Unit>,
        gesture: ListItemGestureBinding = StandardGestures.click
    ): ListItem<T, S> = bindAction(view, key, gesture) { Unit }

    fun <P : Any> bindAction(
        view: View,
        key: ActionKey<P>,
        gesture: ListItemGestureBinding = StandardGestures.click,
        payload: (View) -> P
    ): ListItem<T, S> = apply {
        attachBinding(view, gesture) { _, touched ->
            item?.let { dispatch(ListItemEvent.Custom(it, key, payload(touched), touched)) }
        }
    }

    fun bindAction(
        @IdRes viewId: Int,
        key: ActionKey<Unit>,
        gesture: ListItemGestureBinding = StandardGestures.click
    ): ListItem<T, S> = apply {
        root.findViewById<View>(viewId)?.let { bindAction(it, key, gesture) }
    }

    fun bindActions(vararg pairs: Pair<View, ActionKey<Unit>>) = apply {
        pairs.forEach { (view, key) -> bindAction(view, key) }
    }

    fun unbindAction(view: View, gesture: ListItemGestureBinding? = null) = apply {
        detach(view, gesture)
    }

    fun enableCardClicks(click: Boolean = true, longClick: Boolean = true) = apply {
        if (click) attachBinding(root, StandardGestures.click, ::onCardGesture)
        if (longClick) attachBinding(root, StandardGestures.longClick, ::onCardGesture)
    }

    fun disableCardClicks(click: Boolean = true, longClick: Boolean = true) = apply {
        if (click) detach(root, StandardGestures.click)
        if (longClick) detach(root, StandardGestures.longClick)
    }

    private fun onCardGesture(gesture: ListItemGesture, view: View) {
        val current = item ?: return
        when (gesture) {
            ListItemGesture.Click -> dispatch(ListItemEvent.Click(current, view))
            ListItemGesture.LongClick -> dispatch(ListItemEvent.LongClick(current, view))
            is ListItemGesture.Custom -> Unit
        }
    }

    fun bind(item: T, initial: S = defaultState) {
        this.item = item
        applyState(initial, notify = false)
    }

    fun unbind() {
        val current = item
        if (current != null) {
            renderer?.render(root, current, defaultState)
        }
        item = null
        state = defaultState
    }

    fun release() {
        unbind()
        attachments.toList().forEach { it.detach() }
        attachments.clear()
        listeners.clear()
        guards.clear()
        handlers.clear()
        renderer = null
    }

    fun refresh() {
        val current = item ?: return
        renderer?.render(root, current, state)
    }

    fun update(transform: (S) -> S): Boolean {
        val current = item ?: return false
        val new = transform(state)
        if (new == state) return false
        val event = ListItemEvent.StateChanged(current, state, new)
        if (!guards.all { it.allow(event, state) }) return false
        applyState(new, notify = true)
        return true
    }

    fun <P : Any> triggerAction(key: ActionKey<P>, payload: P, view: View = root) {
        item?.let { dispatch(ListItemEvent.Custom(it, key, payload, view)) }
    }

    fun triggerAction(key: ActionKey<Unit>, view: View = root) {
        triggerAction(key, Unit, view)
    }

    private fun applyState(new: S, notify: Boolean) {
        val old = state
        state = new

        val current = item
        if (current != null) {
            renderer?.render(root, current, new)
            if (notify) {
                val event = ListItemEvent.StateChanged(current, old, new)
                listeners.toList().forEach { it.onEvent(event) }
            }
        }
    }

    private fun dispatch(event: ListItemEvent<T, S>) {
        if (!guards.all { it.allow(event, state) }) return
        listeners.toList().forEach { it.onEvent(event) }
        if (event is ListItemEvent.Custom) {
            handlers[event.key]?.toList()?.forEach { it(event.item, event.payload, event.view) }
        }
    }

    private fun attachBinding(
        view: View,
        binding: ListItemGestureBinding,
        onFire: (ListItemGesture, View) -> Unit
    ) {
        detach(view, binding)

        var lastClickAt = 0L
        val undo = binding.attach(view) gesture@{ gesture, touched ->
            if (gesture is ListItemGesture.Click && clickDebounceMs > 0) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastClickAt < clickDebounceMs) return@gesture
                lastClickAt = now
            }
            onFire(gesture, touched)
        }
        attachments += Attachment(view, binding, undo)
    }

    private fun detach(view: View, binding: ListItemGestureBinding?) {
        val iterator = attachments.iterator()
        while (iterator.hasNext()) {
            val nextItem = iterator.next()
            if (nextItem.view === view && (binding == null || nextItem.binding === binding)) {
                nextItem.detach()
                iterator.remove()
            }
        }
    }
}