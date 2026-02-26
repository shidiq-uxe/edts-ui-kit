# Radio Button Component

The Radio Button component is a customizable Android UI component built by extending Material Design's `MaterialRadioButton`. It provides smooth animations, multiple visual states, and seamless integration with the Selector component for enhanced layouts with titles and subtitles.

---

## Features

- **Material Design Compliance**: Built on top of `MaterialRadioButton` with custom styling
- **Smooth Animations**: Scale and fade animations for state transitions (150-200ms)
- **Multiple Visual States**: Supports unchecked, checked, disabled states with appropriate colors
- **Selector Integration**: Works seamlessly with the Selector component for advanced layouts

---

## Visual States

The Radio Button supports the following states:

| State         | Description    | Preview                                                                | Disabled Preview                                                                |
|---------------|----------------|------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| **Unchecked** | Default state  | ![Radio Button Preview](assets/RadioButton/rb_selector_unchecked.webp) | ![Radio Button Preview](assets/RadioButton/rb_selector_disabled_unchecked.webp) |
| **Checked**   | Selected state | ![Radio Button Preview](assets/RadioButton/rb_selector_checked.webp)   | ![Radio Button Preview](assets/RadioButton/rb_selector_disabled_checked.webp)   |

### Ripple and Scale Animations

The Radio Button features smooth ripple and scale animations for state transitions, enhancing user interaction feedback.

![Radio Button Preview](https://res.cloudinary.com/dpdbzlnhr/image/upload/c_scale,w_400/v1770703690/radiobutton_gif_jkiylr.gif)

---

## Installation

Include the Radio Button component in  layout file by applying the custom drawable to a `MaterialRadioButton`:

```xml
<com.google.android.material.radiobutton.MaterialRadioButton
    android:id="@+id/radioButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Option 1" />
```

For enhanced layouts with title and subtitle support, use the Selector component:

```xml
<id.co.edtslib.uikit.selector.Selector
    android:id="@+id/selector"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:selectorType="radio_button"
    app:titleText="Enable notifications"
    app:subTitleText="Receive updates about your account"
    android:checked="false" />
```
---

## Usage

Apply the custom drawable to a `MaterialRadioButton` in the layout:

```xml
<com.google.android.material.radiobutton.MaterialRadioButton
    android:id="@+id/radioButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Accept terms and conditions" />
```

**Material Radio Button XML Attributes**


Standard `MaterialRadioButton` attributes are supported:

| Attribute Name           | Description                     | Default Value                         |
|--------------------------|---------------------------------|---------------------------------------|
| `android:button`         | Custom radio button drawable    | Required: `@drawable/ic_radio_button` |
| `android:text`           | Label text for the radio button | `null`                                |
| `android:checked`        | Initial checked state           | `false`                               |
| `android:enabled`        | Enable/disable the radio button | `true`                                |
| `android:textColor`      | Color of the label text         | Theme default                         |
| `android:textAppearance` | Text style for the label        | Theme default                         |

### Programmatic Control

Control the radio button state programmatically:

```kotlin
val radioButton = findViewById<MaterialRadioButton>(R.id.radioButton)
radioButton.isChecked = true
radioButton.isEnabled = false

radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
    if (isChecked) {
        // Handle checked state
        Log.d("RadioButton", "Radio button selected")
    }
}
```

---

## Integration with Selector Component

The Radio Button is integrated into the **Selector** component, which provides a unified interface for both Radio Button and Checkbox components with built-in title and subtitle support.

### Selector Component Overview

The `Selector` class is a custom Kotlin component that wraps both `MaterialCheckBox` and `MaterialRadioButton`, allowing you to switch between them using the `SelectorType` enum.

```kotlin
enum class SelectorType {
    CHECKBOX, 
    RADIO_BUTTON
}
```

### Using Radio Button in Selector

#### XML Declaration

```xml
<id.co.edtslib.uikit.selector.Selector
    android:id="@+id/selector"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:selectorType="radio_button"
    app:titleText="Enable notifications"
    app:subTitleText="Receive updates about your account"
    app:titleVisible="true"
    app:subtitleVisible="true"
    android:checked="false"
    android:enabled="true" />
```

**Selector Custom Attributes:**

| Attribute Name        | Description                                   | Default Value           |
|-----------------------|-----------------------------------------------|-------------------------|
| `app:selectorType`    | Set to `"radio_button"` for Radio Button mode | `"checkbox"`            |
| `app:titleText`       | Main label text                               | `"Selector Title"`      |
| `app:subTitleText`    | Secondary description text                    | `"Body text goes here"` |
| `app:titleVisible`    | Show/hide title                               | `true`                  |
| `app:subtitleVisible` | Show/hide subtitle                            | `true`                  |
| `android:checked`     | Initial checked state                         | `false`                 |
| `android:enabled`     | Enable/disable the component                  | `true`                  |

---

### Programmatic Usage with Selector

Configure and control the Selector component in Radio Button mode:

```kotlin
val selector = findViewById<Selector>(R.id.selector)

selector.selectorType = SelectorType.RADIO_BUTTON
selector.isChecked = true
selector.title = "New title"
selector.subtitle = "New subtitle"
selector.isTitleVisible = true
selector.isSubtitleVisible = false
selector.isEnabled = false
selector.toggle()

selector.delegate = object : SelectorDelegate {
    override fun onCheckedChanged(isChecked: Boolean) {
        Log.d("Selector", "Radio button is checked: $isChecked")
    }
    
    override fun onCheckedStateChanged(state: Int) {
        // Not used for radio buttons (checkbox only)
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