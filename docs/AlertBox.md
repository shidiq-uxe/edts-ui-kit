# AlertBox

## Overview

The `AlertBox` class is an easy-to-use, customizable UI component designed to display alert messages within Android applications. This component enhances user experience by providing context-specific alerts with icons, colors, and optional interaction through a close button.

## Key Features

-   **Predefined Alert Types**: Includes `SUCCESS`, `ERROR`, `WARNING`, `INFO`, and `LIGHT` for standardized feedback.
-   **Customizable Attributes**: Allows for setting different styles.
-   **Delegate Pattern Support**: Enables close button interaction with a customizable callback through `AlertBoxDelegate`.

## AlertBox Overview

| AlertBox Style  | Description                                               |
|-----------------|-----------------------------------------------------------|
| **Light**       | <img src="./assets/AlertBox/Light.webp" width="400">      |
| **Info**        | <img src="./assets/AlertBox/Info.webp" width="400">       |
| **Success**     | <img src="./assets/AlertBox/Success.webp" width="400">    |
| **Warning**     | <img src="./assets/AlertBox/Warning.webp" width="400">    |
| **Error**       | <img src="./assets/AlertBox/Error.webp" width="400">      |
| **With Button** | <img src="./assets/AlertBox/WithButton.webp" width="400"> |


| Close Animation                                                                                           |
|-----------------------------------------------------------------------------------------------------------|
| ![Close](https://res.cloudinary.com/dmduc9apd/image/upload/v1731035526/Alertbox/deo9aizehttlr52a4fyi.gif) |

>In order to use animation shown above, use `animateLayoutChanges="true"` in Parent ViewGroup of the AlertBox.

## AlertType Enum

The `AlertType` enum determines the appearance of the `AlertBox`. Available types:

-   `SUCCESS`
-   `ERROR`
-   `WARNING`
-   `INFO`
-   `LIGHT`

Each type comes with specific:

-   **Background color**
-   **Text color**
-   **Stroke color**
-   **Icon**

## Usage Example

### XML Layout

```xml
<id.co.edtslib.uikit.alert.AlertBox
    android:id="@+id/alertBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:alertType="error"
    app:text="This is an error alert." />
```

### Kotlin Code

```kotlin
alertBox.apply {
    alertType = AlertBox.AlertType.SUCCESS
    text = "Your operation was successful!"
    delegate = object : AlertBoxDelegate {
        override fun onCloseClickListener(view: View) {
            // Handle close button click
        }
        override fun onButtonClickListener(view: View) {
            // Handle action button click
        }
    }
}
```

## Adding Span Content
You could adjust span of the message using:
```kotlin
buildHighlightedMessage(
    context = this@GuidelinesCartActivity,
    message = "This is regular text while this is Bold and H4",
    highlightedMessages = listOf("Bold", "H4"),
    highlightedTextAppearance = listOf(
        TextStyleKey.B4_SEMIBOLD.get(context, UIKitR.color.black_70),
        TextStyleKey.H4_SEMIBOLD.get(context, UIKitR.color.black_70),
    )
)
```
>See more about this function inside [StringBuilderExtension](https://github.com/shidiq-uxe/edts-ui-kit/blob/main/uikit/src/main/java/id/co/edtslib/uikit/utils/StringBuilderExtension.kt)


## Customization Options

| Property               | Description                                                                 |
|------------------------|-----------------------------------------------------------------------------|
| `alertType`            | Enum to define alert style (`SUCCESS`, `ERROR`, `WARNING`, `INFO`, `LIGHT`) |
| `text`                 | Message to display                                                          |
| `textColor`            | Message color                                                               |
| `textAppearance`       | Style for the text appearance                                               |
| `cornerRadius`         | Radius for card corners                                                     |
| `strokeWidth`          | Width of the border stroke                                                  |
| `strokeColor`          | Color of the border stroke                                                  |
| `alertBackgroundColor` | Background color of the alert                                               |
| `startIcon`            | Drawable shown on the left                                                  |
| `startIconTint`        | Tint for the start icon                                                     |
| `isStartIconVisible`   | Whether to show start icon                                                  |
| `closeIcon`            | Drawable for the close icon                                                 |
| `closeIconTint`        | Tint for the close icon                                                     |
| `isCloseIconVisible`   | Whether to show the close icon                                              |
| `buttonText`           | Text for the optional action button                                         |
| `buttonType`           | Enum for button style (e.g., `FILLED`, custom)                              |
| `isButtonVisible`      | Whether to show the button                                                  |
| `isHtml`               | Parse text to html                                                          |

## Attributes Reference

| XML Attribute            | Type      | Description              |
|--------------------------|-----------|--------------------------|
| `app:alertType`          | enum      | Alert style              |
| `app:text`               | string    | Alert message            |
| `app:textColor`          | color     | Message text color       |
| `app:textAppearance`     | style     | Text appearance          |
| `app:strokeWidth`        | dimension | Border thickness         |
| `app:strokeColor`        | color     | Border color             |
| `app:cornerRadius`       | dimension | Corner radius            |
| `app:backgroundColor`    | color     | Background color         |
| `app:startIcon`          | drawable  | Icon on the left         |
| `app:startIconTint`      | color     | Tint for start icon      |
| `app:isStartIconVisible` | boolean   | Toggle start icon        |
| `app:closeIcon`          | drawable  | Close icon drawable      |
| `app:closeIconTint`      | color     | Tint for close icon      |
| `app:isCloseIconVisible` | boolean   | Toggle close icon        |
| `app:buttonText`         | string    | Action button text       |
| `app:isButtonVisible`    | boolean   | Toggle button visibility |
| `app:isHtml`             | boolean   | Parse text to html       |

## Delegate Interface

Implement `AlertBoxDelegate` to handle interaction:

```kotlin
interface AlertBoxDelegate { 
    fun onCloseClickListener(view: View)
    fun onButtonClickListener(view: View)
}
```

## Customization and Styling

The `AlertBox` can be styled using custom attributes or by setting properties programmatically. To match the app's visual design, ensure consistency with text appearance and color themes.
## Inline action and start-icon alignment

The inline action is separate from the existing button below the message. It is hidden by
default; existing `buttonText`, `buttonType`, `isButtonVisible`, and button callbacks retain
their behavior. The message accepts `CharSequence`, including styled spans when `isHtml`
is false.

| Property | Default | Purpose |
|---|---|---|
| `startIconVerticalAlignment` | `TOP` | `TOP`, `CENTER`, or `BOTTOM`, relative to the complete message block. |
| `actionText` | Empty | Inline action label; supports `CharSequence`. |
| `actionTextAppearance` | Inter Semibold small button | Typography for the inline label. |
| `actionTextColor` | Primary blue | Color integer for the label. |
| `actionMaxLines` | `1` | Positive maximum line count. |
| `actionEllipsize` | `END` | Truncation mode; `null` disables truncation. |
| `actionEndIcon` | `null` | Optional drawable at the logical end of the label. |
| `actionEndIconTint` | `null` | Optional color integer for the icon. |
| `actionIconPadding` | 4dp in pixels | Space between label and icon. |
| `isActionVisible` | `false` | Showing/hiding the inline action; hidden space is returned to the message. |

All properties have matching XML attributes. XML `actionEllipsize` supports `none`, `start`,
`middle`, and `end`. Icon alignment preserves the legacy top offset by default;
`showOpticalSpacing()` controls that offset only for `TOP` alignment.

```xml
<id.co.edtslib.uikit.alert.AlertBox
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:text="@string/promotion_message"
    app:startIconVerticalAlignment="center"
    app:isCloseIconVisible="false"
    app:actionText="@string/edit"
    app:actionEndIcon="@drawable/ic_chevron_right_16"
    app:isActionVisible="true" />
```

```kotlin
alertBox.delegate = object : AlertBoxDelegate {
    override fun onCloseClickListener(view: View) = Unit
    override fun onButtonClickListener(view: View) = Unit
    override fun onActionClickListener(view: View) {
        openPromotionSelection()
    }
}
```

`onActionClickListener` has a default no-op implementation, so existing delegate
implementations do not need to implement it. Inline and lower-button callbacks are independent.
