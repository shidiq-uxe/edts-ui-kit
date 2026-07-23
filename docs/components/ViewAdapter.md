# ViewAdapter

*Use this when you have a simple list and want to create views in code (no XML layout). For example: a list of colored squares, a list of text labels, or any view you build without ViewBinding.*

**Not sure which adapter to pick?** → If you have an XML layout and ViewBinding, use [BaseAdapter](BaseAdapter.md). If one list has multiple item styles, use [BaseMultiTypeAdapter](BaseMultiTypeAdapter.md).

---

## Usage

### Basic Example

```kotlin
// Step 1: Create the adapter
val adapter = ViewAdapter.adapterOf<String, TextView>(
    viewFactory = { context -> TextView(context) },
    configure = { position, view, item ->
        view.text = item
    }
)

// Step 2: Set it on the RecyclerView
recyclerView.adapter = adapter

// Step 3: Give it data
adapter.items = listOf("Apple", "Banana", "Cherry")
```

### With Click

```kotlin
adapter.setOnItemClickListener { view, item, position ->
    Toast.makeText(context, "Clicked: $item", Toast.LENGTH_SHORT).show()
}
```

---

## What is `Diff`?

When your list changes, the adapter needs to know *which* items changed so it only updates those. You tell it by providing two checks:

```kotlin
val adapter = ViewAdapter.adapterOf(
    viewFactory = { context -> TextView(context) },
    configure = { position, view, item -> view.text = item.name },
    diff = BaseAdapter.Diff(
        areItemsTheSame = { old, new -> old.id == new.id },       // "Is this the same row?"
        areContentsTheSame = { old, new -> old == new }           // "Did the data change?"
    )
)
```

**Rule of thumb:** If your data class has an `id`, compare by `id` for `areItemsTheSame`. Always compare the full object for `areContentsTheSame` unless you have a reason not to.

If you skip `diff`, the adapter falls back to `old == new` — this only works if your items never change. Most of the time you need to provide it.

---

## All Options (`adapterOf`)

| Parameter | What it does | Example |
|---|---|---|
| `viewFactory` | Creates the view for each row | `{ context -> TextView(context) }` |
| `configure` | Puts data into the view | `{ pos, view, item -> view.text = item }` |
| `diff` | Tells the adapter how to check if data changed *(optional)* | `BaseAdapter.Diff(...)` |
| `itemList` | Starting data *(optional)* | `listOf(...)` |

---

## Properties

| Property | Description |
|---|---|
| `items` | Get or set the full list. Setting it automatically updates the RecyclerView. |

---

## Methods

| Method | Description |
|---|---|
| `setOnItemClickListener { view, item, position -> ... }` | Called when any item is tapped |

---

## Common Recipes

### List of strings
```kotlin
ViewAdapter.adapterOf<String, TextView>(
    viewFactory = { context -> TextView(context) },
    configure = { _, view, text -> view.text = text }
)
```

### List of data class with click
```kotlin
data class Person(val id: Int, val name: String)

ViewAdapter.adapterOf(
    viewFactory = { context -> TextView(context) },
    configure = { _, view, person -> view.text = person.name },
    diff = BaseAdapter.Diff(
        areItemsTheSame = { old, new -> old.id == new.id },
        areContentsTheSame = { old, new -> old == new }
    )
).apply {
    setOnItemClickListener { _, person, _ ->
        // navigate to detail screen
    }
}
```

---

- Uses `AsyncListDiffer` so list updates don't block the UI.
