# BaseMultiTypeAdapter

*Use this when one RecyclerView needs to show different types of items — for example, headers, product cards, and banners all mixed in the same list. Each item type can have its own layout and binding logic.*

**Not sure which adapter to pick?** → If every row has the same layout, use [BaseAdapter](BaseAdapter.md). If you don't use XML layouts at all, use [ViewAdapter](ViewAdapter.md).

---

## Usage

There are two ways to use it:

1. **`multiTypeAdapter` factory** — shorter, for simple cases
2. **Subclass** — when you want a reusable adapter class

### Way 1: Factory Function

```kotlin
// Define type IDs — just pick integers that don't clash
const val TYPE_HEADER = 0
const val TYPE_PRODUCT = 1

val adapter = multiTypeAdapter(
    diffCallback = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(old: Any, new: Any): Boolean {
            // Compare by type + id so we don't mix up different item types
            return if (old is HeaderItem && new is HeaderItem) old.id == new.id
                   else if (old is ProductItem && new is ProductItem) old.id == new.id
                   else false
        }
        override fun areContentsTheSame(old: Any, new: Any) = old == new
    },
    viewTypeConfig = { item ->
        when (item) {
            is HeaderItem -> TYPE_HEADER
            is ProductItem -> TYPE_PRODUCT
            else -> throw IllegalStateException("Unknown item type: $item")
        }
    },
    bindingConfig = {
        // Register each type once — one layout, one bind function
        registerViewType<ItemHeaderBinding>(TYPE_HEADER) { position, binding, item ->
            val header = item as HeaderItem
            binding.tvHeader.text = header.title
        }
        registerViewType<ItemProductBinding>(TYPE_PRODUCT) { position, binding, item ->
            val product = item as ProductItem
            binding.tvName.text = product.name
            binding.tvPrice.text = product.price
        }
    }
)

recyclerView.adapter = adapter
adapter.items = listOf(
    HeaderItem(1, "Today's Deals"),
    ProductItem(2, "Milk", "$3"),
    ProductItem(3, "Bread", "$2")
)
```

### Way 2: Subclass (Reusable)

```kotlin
class ProductListingAdapter : BaseMultiTypeAdapter<Any>(
    diffCallback = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(old: Any, new: Any): Boolean {
            return if (old is HeaderItem && new is HeaderItem) old.id == new.id
                   else if (old is ProductItem && new is ProductItem) old.id == new.id
                   else false
        }
        override fun areContentsTheSame(old: Any, new: Any) = old == new
    }
) {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_PRODUCT = 1
    }

    // Register view types in init
    init {
        registerViewType<ItemHeaderBinding>(TYPE_HEADER) { position, binding, item ->
            val header = item as HeaderItem
            binding.tvHeader.text = header.title
        }
        registerViewType<ItemProductBinding>(TYPE_PRODUCT) { position, binding, item ->
            val product = item as ProductItem
            binding.tvName.text = product.name
            binding.tvPrice.text = product.price
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HeaderItem -> TYPE_HEADER
            is ProductItem -> TYPE_PRODUCT
            else -> -1
        }
    }
}

// Use it
val adapter = ProductListingAdapter()
recyclerView.adapter = adapter
```

---

## What is a View Type?

A view type is just a number you pick to tell the RecyclerView which layout to use for which row. You can use any integers — just make sure they don't overlap. Simple `0, 1, 2, 3...` is fine.

The adapter needs two pieces for each view type:
1. **Which layout to inflate** — handled automatically when you use `registerViewType<YourBinding>(typeId)`
2. **How to fill the layout with data** — the bind lambda you provide

---

## DiffCallback for Multiple Types

Since your list has mixed types, the `diffCallback` must handle all of them:

```kotlin
object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(old: Any, new: Any): Boolean {
        // Different types can never be the same item
        return old::class == new::class && old is HasId && new is HasId && old.id == new.id
    }
    override fun areContentsTheSame(old: Any, new: Any) = old == new
}
```

A common pattern: make all your item types implement a shared interface with an `id`:

```kotlin
interface ListItem { val id: Int }
data class HeaderItem(override val id: Int, val title: String) : ListItem
data class ProductItem(override val id: Int, val name: String, val price: String) : ListItem
```

Then your diff callback becomes simpler:

```kotlin
override fun areItemsTheSame(old: Any, new: Any): Boolean {
    return old is ListItem && new is ListItem && old.id == new.id
}
```

---

## Factory Function Parameters

| Parameter | What it does |
|---|---|
| `diffCallback` | Tells the adapter how to compare items |
| `viewTypeConfig` | Given an item, returns which view type to use (0, 1, 2...) |
| `bindingConfig` | Registers each view type — its layout and how to fill it |
| `onViewDetachedFromWindow` | *(optional)* Called when a row scrolls off screen. Useful for stopping animations or cleanup |

---

## Properties

| Property | Description |
|---|---|
| `items` | Get or set the full list. Updates the RecyclerView automatically. |

---

## Methods

| Method | Description |
|---|---|
| `registerViewType<B>(typeId) { pos, binding, item -> ... }` | Register a type with its binding class and data-binding logic |
| `getItemViewType(position)` | Override this in subclasses — return the type ID for a position |

---

## Notes

- There is no built-in click listener like `BaseAdapter`. Attach click listeners directly to views inside each `registerViewType` bind block.
- For single-view-type lists, use `BaseAdapter` — it is simpler and type-safe.
