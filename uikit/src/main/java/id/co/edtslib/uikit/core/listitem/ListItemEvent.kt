package id.co.edtslib.uikit.core.listitem

import android.view.View

/** Identity token for an action. Matched by reference. */
class ActionKey<P : Any>(val name: String) {
    override fun toString() = "ActionKey($name)"
}

sealed interface ListItemEvent<out T : Any, out S : Any> {
    val item: T

    data class Click<T : Any>(override val item: T, val view: View) : ListItemEvent<T, Nothing>

    data class LongClick<T : Any>(override val item: T, val view: View) : ListItemEvent<T, Nothing>

    data class Custom<T : Any>(
        override val item: T,
        val key: ActionKey<*>,
        val payload: Any,
        val view: View
    ) : ListItemEvent<T, Nothing> {

        @Suppress("UNCHECKED_CAST")
        fun <P : Any> payloadOf(key: ActionKey<P>): P {
            check(this.key === key) { "Expected $key but received ${this.key}" }
            return payload as P
        }
    }

    data class StateChanged<T : Any, S : Any>(
        override val item: T,
        val oldState: S,
        val newState: S
    ) : ListItemEvent<T, S>
}