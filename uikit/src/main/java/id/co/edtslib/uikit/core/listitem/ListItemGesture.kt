package id.co.edtslib.uikit.core.listitem

import android.view.View

sealed interface ListItemGesture {
    data object Click : ListItemGesture
    data object LongClick : ListItemGesture
    data class Custom(val name: String) : ListItemGesture
}

fun interface ListItemGestureBinding {
    fun attach(view: View, onGesture: (ListItemGesture, View) -> Unit): () -> Unit
}

object StandardGestures {

    val click = ListItemGestureBinding { view, onGesture ->
        val wasClickable = view.isClickable
        view.setOnClickListener { tapped -> onGesture(ListItemGesture.Click, tapped) }
        val detach: () -> Unit = {
            view.setOnClickListener(null)
            view.isClickable = wasClickable
        }
        detach
    }

    val longClick = ListItemGestureBinding { view, onGesture ->
        val wasLongClickable = view.isLongClickable
        view.setOnLongClickListener { pressed ->
            onGesture(ListItemGesture.LongClick, pressed)
            true
        }
        val detach: () -> Unit = {
            view.setOnLongClickListener(null)
            view.isLongClickable = wasLongClickable
        }
        detach
    }
}