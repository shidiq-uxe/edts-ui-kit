# Checkbox

## Overview

Checkbox component is a customizable Android UI component built by extending Material Design's `MaterialCheckBox` through XML drawables. The component features smooth animations, multiple visual states including indeterminate support, and is integrated into the Selector component for flexible usage.

---

## Features

- **Material Design Compliance**: Built on top of `MaterialCheckBox` with custom styling
- **Smooth Animations**: Scale and fade animations for all state transitions (150-200ms)
- **Three-State Support**: Unchecked, checked, and indeterminate states
- **Multiple Visual States**: Comprehensive support for all enabled/disabled combinations
- **Animation Transitions**: Smooth animations between all possible state changes
- **Selector Integration**: Works seamlessly with the Selector component for advanced layouts

---

## Visual States

The Checkbox supports the following states:

| State             | Description             | Preview                                                            | Disabled Preview                                                            |
|-------------------|-------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------------------|
| **Unchecked**     | Default state           | ![Checkbox Preview](assets/Checkbox/c_selector_unchecked.webp)     | ![Checkbox Preview](assets/Checkbox/c_selector_unchecked_disabled.webp)     |
| **Checked**       | Selected state          | ![Checkbox Preview](assets/Checkbox/c_selector_checked.webp)       | ![Checkbox Preview](assets/Checkbox/c_selector_checked_disabled.webp)       |
| **Indeterminate** | Partial selection state | ![Checkbox Preview](assets/Checkbox/c_selector_indeterminate.webp) | ![Checkbox Preview](assets/Checkbox/c_selector_indeterminate_disabled.webp) |

#### Ripple and Scale Animations

The Checkbox features smooth ripple and scale animations for state transitions, enhancing user interaction feedback.

![Checkbox Preview](https://res.cloudinary.com/dpdbzlnhr/image/upload/c_scale,w_400/v1770703740/checkbox_gif_3_ikiclr.gif)

---


## Installation

Include the Checkbox component in your layout file by applying the custom drawable to a `MaterialCheckBox`:

```xml
<com.google.android.material.checkbox.MaterialCheckBox
    android:id="@+id/checkbox"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Accept terms and conditions" />
```

For enhanced layouts with title and subtitle support, use the `Selector` component:

```xml
<id.co.edtslib.uikit.selector.Selector
    android:id="@+id/selector"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:selectorType="checkbox"
    app:titleText="Enable notifications"
    app:subTitleText="Receive updates about your account"
    android:checked="false" />
```

---

## Usage

Apply the custom drawable to a `MaterialCheckBox` in your layout:

```xml
<com.google.android.material.checkbox.MaterialCheckBox
    android:id="@+id/checkbox"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Accept terms and conditions" />
```



**Material Checkbox XML Attributes**

Standard `MaterialCheckBox` attributes plus indeterminate support:

| Attribute Name           | Description                                     | Default Value                     |
|--------------------------|-------------------------------------------------|-----------------------------------|
| `android:button`         | Custom checkbox drawable                        | Required: `@drawable/ic_checkbox` |
| `android:text`           | Label text for the checkbox                     | `null`                            |
| `android:checked`        | Initial checked state (boolean)                 | `false`                           |
| `app:checkedState`       | Initial state (unchecked/checked/indeterminate) | `unchecked`                       |
| `android:enabled`        | Enable/disable the checkbox                     | `true`                            |
| `android:textColor`      | Color of the label text                         | Theme default                     |
| `android:textAppearance` | Text style for the label                        | Theme default                     |

---

### Programmatic Control

```kotlin
checkbox.isChecked = true
checkbox.checkedState = MaterialCheckBox.STATE_INDETERMINATE
checkbox.isEnabled = false

checkbox.addOnCheckedStateChangedListener { checkBox, state ->
    when (state) {
        MaterialCheckBox.STATE_CHECKED -> { /* Handle checked */ }
        MaterialCheckBox.STATE_UNCHECKED -> { /* Handle unchecked */ }
        MaterialCheckBox.STATE_INDETERMINATE -> { /* Handle indeterminate */ }
    }
}
```

---

## Integration with Selector Component

The Checkbox is integrated into the **Selector** component, which provides a unified interface for both Checkbox and Radio Button components with title and subtitle support.

### Selector Component Overview

The `Selector` class is a custom Kotlin component that wraps both `MaterialCheckBox` and `MaterialRadioButton`, allows to switch between them using the `SelectorType` enum.

```kotlin
enum class SelectorType {
    CHECKBOX, 
    RADIO_BUTTON
}
```

### Using Checkbox in Selector

#### XML Declaration

```xml
<id.co.edtslib.uikit.selector.Selector
    android:id="@+id/selector"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:selectorType="checkbox"
    app:titleText="Enable notifications"
    app:subTitleText="Receive updates about your account"
    app:titleVisible="true"
    app:subtitleVisible="true"
    android:checked="false"
    app:checkedState="unchecked"
    android:enabled="true" />
```


**Selector Custom Attributes:**

| Attribute Name        | Description                                      | Default Value           |
|-----------------------|--------------------------------------------------|-------------------------|
| `app:selectorType`    | Set to `"checkbox"` for Checkbox mode (default)  | `"checkbox"`            |
| `app:titleText`       | Main label text                                  | `"Selector Title"`      |
| `app:subTitleText`    | Secondary description text                       | `"Body text goes here"` |
| `app:titleVisible`    | Show/hide title                                  | `true`                  |
| `app:subtitleVisible` | Show/hide subtitle                               | `true`                  |
| `android:checked`     | Simple boolean checked state                     | `false`                 |
| `app:checkedState`    | Detailed state (unchecked/checked/indeterminate) | `unchecked`             |
| `android:enabled`     | Enable/disable the component                     | `true`                  |


#### Programmatic Usage

```kotlin
val selector = findViewById<Selector>(R.id.selector)

selector.selectorType = SelectorType.CHECKBOX
selector.title = "New title"
selector.subtitle = "New subtitle"
selector.isTitleVisible = true
selector.isSubtitleVisible = false
selector.isEnabled = false
selector.isChecked = true
selector.toggle()

val checkbox = selector.findViewById<MaterialCheckBox>(R.id.checkbox)
checkbox.checkedState = MaterialCheckBox.STATE_INDETERMINATE

selector.delegate = object : SelectorDelegate {
    override fun onCheckedChanged(isChecked: Boolean) {
        // Not used for checkboxes (radio button only)
    }
    
    override fun onCheckedStateChanged(state: Int) {
        when (state) {
            MaterialCheckBox.STATE_CHECKED -> {
                Log.d("Selector", "Checkbox is checked")
            }
            MaterialCheckBox.STATE_UNCHECKED -> {
                Log.d("Selector", "Checkbox is unchecked")
            }
            MaterialCheckBox.STATE_INDETERMINATE -> {
                Log.d("Selector", "Checkbox is indeterminate")
            }
        }
    }
}
```

---

## Dependencies

Ensure that your project includes the necessary dependencies for `MaterialButton`

```gradle
implementation 'com.google.android.material:material:1.9.0'
```

---