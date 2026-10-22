package id.co.edtslib.uikit.core.listitem

import android.view.View

fun interface ListItemListener<T : Any, S : Any> {
    fun onEvent(event: ListItemEvent<T, S>)
}

/** Sees the current state, so a guard can say "ignore taps while loading". */
fun interface ListItemGuard<T : Any, S : Any> {
    fun allow(event: ListItemEvent<T, S>, state: S): Boolean
}

fun interface ListItemRenderer<T : Any, S : Any> {
    fun render(root: View, item: T, state: S)
}