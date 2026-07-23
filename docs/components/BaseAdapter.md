# BaseAdapter

*Use this when you have a list of items and you already made an XML layout + ViewBinding for each row. This is the most common adapter in the project.*

**Not sure which adapter to pick?** → If you don't have an XML layout (you build views in code), use [ViewAdapter](ViewAdapter.md). If one list has multiple item styles, use [BaseMultiTypeAdapter](BaseMultiTypeAdapter.md).

---

## Usage

### Basic Example

Imagine you have this data class and layout:

```kotlin
data class Product(val id: Int, val name: String, val price: String)
```

```xml
<!-- item_product.xml -->
<LinearLayout ...>
    <TextView android:id="@+id/tvName" ... />
    <TextView android:id="@+id/tvPrice" ... />
</LinearLayout>
```

Here is how you use the adapter:

```kotlin
// Step 1: Create the adapter
val adapter = BaseAdapter.adapterOf(
    register = BaseAdapter.Register(
        onBindHolder = { position, item, binding, diffUtil ->
            (binding as ItemProductBinding).apply {
                tvName.text = item.name
                tvPrice.text = item.price
            }
        }
    ),
    diff = BaseAdapter.Diff(
        areItemsTheSame = { old, new -> old.id == new.id },
        areContentsTheSame = { old, new -> old == new }
    )
)

// Step 2: Set it on the RecyclerView
recyclerView.adapter = adapter

// Step 3: Give it data
adapter.items = listOf(
    Product(1, "Milk", "$3"),
    Product(2, "Bread", "$2")
)
```

### With Click

```kotlin
adapter.setOnItemClickListener { binding, item, position ->
    // binding is ItemProductBinding, item is Product
    Toast.makeText(context, "Clicked: ${item.name}", Toast.LENGTH_SHORT).show()
}
```

### Shimmer (Loading Placeholder)

```kotlin
// Show 5 shimmer placeholders while data loads
val shimmer = BaseAdapter.shimmerAdapter<ItemProductBinding>(size = 5)
recyclerView.adapter = shimmer

// When data arrives, swap to the real adapter
recyclerView.adapter = realAdapter
```

---

## What is `Diff`?

Same as in ViewAdapter. You tell the adapter how to compare items so it only updates what changed:

```kotlin
BaseAdapter.Diff(
    areItemsTheSame = { old, new -> old.id == new.id },       // "Same row?"
    areContentsTheSame = { old, new -> old == new }           // "Same data?"
)
```

**Always provide this.** The default (`old == new`) only works for simple cases.

---

## `onBindHolder` Parameters

The binding function gives you these:

| Parameter | What it is |
|---|---|
| `position` | The position in the list (0, 1, 2, ...) |
| `item` | The data object for this row |
| `binding` | Your ViewBinding (you need to cast it with `as`) |
| `diffUtil` | The differ — you rarely need this |

---

## Layout Options

You can control how big each row is relative to the RecyclerView, or add spacing around each row: *(all optional)*

```kotlin
BaseAdapter.adapterOf(
    register = ...,
    diff = ...,
    params = BaseAdapter.Params(
        widthPercent = 100.0,                         // row takes full width of RecyclerView
        heightPercent = 50.0,                         // row takes half the height
        margin = BaseAdapter.Params.Margin(
            left = 16, top = 8, right = 16, bottom = 8
        )
    )
)
```

| Setting | Default | Meaning |
|---|---|---|
| `widthPercent` | `0` | Width as % of RecyclerView. `0` = wrap content, `100` = full width |
| `heightPercent` | `0` | Height as % of RecyclerView. `0` = wrap content |
| `margin` | `0` all sides | Spacing around each row (in pixels) |

---

## Properties

| Property | Description |
|---|---|
| `items` | Get or set the full list. Updates the RecyclerView automatically. |

---

## Methods

| Method | Description |
|---|---|
| `setOnItemClickListener { binding, item, position -> ... }` | Called when any row is tapped |

---

## Common Recipes

### Simple list with click and diff
```kotlin
BaseAdapter.adapterOf(
    register = BaseAdapter.Register(
        onBindHolder = { _, item, binding, _ ->
            (binding as ItemRowBinding).apply {
                tvTitle.text = item.title
            }
        }
    ),
    diff = BaseAdapter.Diff(
        areItemsTheSame = { old, new -> old.id == new.id },
        areContentsTheSame = { old, new -> old == new }
    )
).apply {
    setOnItemClickListener { _, item, _ ->
        // handle click
    }
}
```

### Full-width rows with spacing
```kotlin
BaseAdapter.adapterOf(
    register = ...,
    diff = ...,
    params = BaseAdapter.Params(
        widthPercent = 100.0,
        margin = BaseAdapter.Params.Margin(
            top = 12, bottom = 12
        )
    )
)
```
