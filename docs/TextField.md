TextField
=========

The `TextField` is a custom text input component that extends Android's `TextInputLayout`. It is designed to provide a versatile text input solution with built-in validation, error handling, and customizable input types. This documentation describes how to integrate and use the component in your project.

* * * * *

Overview
--------

The `TextField` component is built on top of Material Design's `TextInputLayout` and wraps a `TextInputEditText`. It supports multiple input types (e.g., text, password, pin, phone, email, OTP) that have been developed in `EDTSLibs` and provides:

-   Built-in error handling with custom animations and haptic feedback.
-   Configurable maximum character lengths.
-   Dynamic hint modification for required fields (appends an asterisk `*` to the hint).
-   Delegated callbacks to notify when the text value changes.

* * * * *

Common Design System Use Cases
------------------------------

The example XML provided here: [Source Code](https://github.com/shidiq-uxe/edts-ui-kit/blob/main/app/src/main/res/layout/activity_textfield.xml) demonstrates various common scenarios within your Design System:

1.  **Standard Input:**\
    A simple text field with a placeholder.\
    <img src="assets/TextField/tf_default.jpg" width="600"/>

2. **Input with Start Icon:**\
    An input field displaying a start icon.\
   <img src="assets/TextField/tf_start_icon.jpg" width="600"/>

3. **Input with Both Start and End Icons:**\
    A configuration that uses custom end icon mode.\
   <img alt="Start End Icon Example" src="assets/TextField/tf_start_end_icon.jpg" width="600"/>

4. **Error State Input:**\
    A field that will display error states along with icons.\
   <img alt="Error Example" src="assets/TextField/tf_error_counter.jpg" width="600"/>

5. **Password Input:**\
    An input field configured for password entry, which enables an end icon for toggling visibility.\
   <img alt="Password Example" src="assets/TextField/tf_password.jpg" width="600"/>

6. **Input with Helper Text and Character Counter:**\
    Fields that support additional helper text and a counter to show the remaining characters.\
   <img alt="Counter Example" src="assets/TextField/tf_counter.jpg" width="600"/>

7. **Disabled Input:**\
    A text field that is disabled, which still displays helper text and a character counter.\
   <img alt="Disabled Example" src="assets/TextField/tf_disabled.jpg" width="600"/>

8.  **Loading State Input:**\
    A text field intended to display a loading state while showing supporting text.\
    ![Loading Example](assets/TextField/tf_loading.gif)

Features
--------

-   **Input Types:**\
    Supports multiple types through the `InputType` enum:

    -   `Text`
    -   `Password`
    -   `Pin`
    -   `Phone`
    -   `Email`
    -   `OTP`
-   **IME Options:**\
    Configurable through the `ImeOption` enum:

    -   `Next`
    -   `Send`
    -   `Search`
    -   `Done`
-   **Validation and Error Styling:**\
    Automatically toggles error state and helper text appearance based on validation results. Also triggers a vibration animation and haptic feedback when an error is set.

-   **Text Change Delegate:**\
    Use the `delegate` property to listen for text changes.

-   **Max Length Filtering:**\
    Automatically applies or removes character length filters based on the `maxLength` property.

* * * * *

Usage
-----

### XML Integration

To use `TextField` in your XML layouts, include the component along with its custom attributes.

```xml
<id.co.edtslib.uikit.textfield.TextField
    style="@style/Widget.EDTS.UIKit.TextInputLayout.LabelInside"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/s"
    app:startIconDrawable="@drawable/ic_placeholder_medium_24"
    app:endIconDrawable="@drawable/ic_placeholder_medium_24"
    app:endIconMode="custom"
    android:hint="Input Normal"
    app:placeholderText="Start End Icon Placeholder" />
```

*Note: The actual attribute names and acceptable integer values correspond to the values defined in your `R.styleable.TextField` and enum order. Adjust these as needed when you provide the XML details.*

### Programmatic Usage

You can also configure the `TextField` in your Kotlin code:

```kotlin
val textField = binding.tfInput.apply {
    // Set the delegate to listen for text changes
    delegate = object : TextFieldDelegate {
        override fun onValueChange(value: String?) {
            // Handle the text change
            println("New value: $value")
        }
    }

    // Set the desired input type (e.g., Email)
    inputType = TextField.InputType.Email

    // Set the IME option (e.g., Done)
    imeOption = TextField.ImeOption.Done

    // Configure maximum allowed length (0 removes filter)
    maxLength = 50
}
```

* * * * *

Custom Attributes
-----------------

When using the component in XML, you can customize it with several attributes:

-   **`app:fieldInputType`**\
    *(Integer)*\
    Sets the type of input. Maps to the `InputType` enum.\
    **Values:**

    -   `Text`
    -   `Password`
    -   `Pin`
    -   `Phone`
    -   `Email`
    -   `OTP`
-   **`app:fieldImeOptions`**\
    *(Integer)*\
    Sets the IME action for the input field. Maps to the `ImeOption` enum.\
    **Values:**

    -   `Next`
    -   `Send`
    -   `Search`
    -   `Done`
-   **`app:fieldMaxLength`**\
    *(Integer)*\
    Defines the maximum number of characters allowed. Set to `0` to remove the limit.

-   **`app:isFieldRequired`**\
    *(Boolean)*\
    When set to true, the component appends an asterisk (`*`) to the hint to indicate that the field is required.

-   **`app/android:[MaterialAttributes]`**\
    Other TextInputLayout atrributes made by [MaterialDesign](https://github.com/material-components/material-components-android/blob/master/docs/components/TextField.md).\

* * * * *

Properties and Methods
----------------------

### Properties

-   **`delegate: TextFieldDelegate?`**\
    Delegate to receive callbacks when the text changes.

-   **`isFieldRequired: Boolean`**\
    Indicates if the field is required. If true, an asterisk is appended to the hint.

-   **`inputType: InputType`**\
    Defines the type of input. Setting this property configures both the underlying edit text's input type and the end icon mode (for example, enabling password toggle for passwords).

-   **`imeOption: ImeOption`**\
    Sets the IME action button on the soft keyboard.

-   **`maxLength: Int`**\
    Specifies the maximum number of characters. When greater than 0, a length filter is applied.

-   **`textInputError: TextView?`**\
    Returns the underlying error text view (if available), allowing further customization if needed.

### Key Methods

-   **`setError(errorText: CharSequence?)`**\
    Sets an error message on the text field. This method:

    -   Updates the helper text and its color.
    -   Applies a vibration animation and haptic feedback.
    -   Adjusts the error styling based on input type (e.g., for OTP inputs).
-   **`isHelperTextEnabled(): Boolean`**\
    Returns whether helper text is currently enabled. This method also ensures the container padding is correctly set.

*Internal methods such as `overrideCollapsingTextHelperErrorColor`, `init`, `setContainerPadding`, `addFilter`, and `removeMaxLengthFilter` manage various aspects of the view but are not intended for external use.*

* * * * *

Error Handling
--------------

When an error is set using `setError`, the component:

-   Changes the helper text color (red for errors, neutral for valid states).
-   Applies an error flag to enable error styling.
-   For OTP input types, changes the text color to indicate an error.
-   Adjusts the line height of the error text to meet design specifications.
-   Triggers a vibration animation and haptic feedback to notify the user.

* * * * *

Customization & Styling
-----------------------

-   **Required Field Indicator:**\
    If `isFieldRequired` is true, the component uses a helper function to append an read asterisk (`*`) to the hint. The asterisk is styled using a custom `TextStyle` (e.g., red color for emphasis).

-   **Input Filters:**\
    The component automatically adds or removes a length filter based on the `maxLength` property.

-   **End Icon Behavior:**\
    For password and pin input types, an end icon toggle is enabled to show/hide the text. For other input types, the end icon mode remains unchanged.

-   **Dynamic Padding:**\
    The container adjusts its padding dynamically, especially when an error message is displayed, to match the design system specifications.

* * * * *

Notes & Requirements
-----

-   **Customization:**\
    While many aspects of the `TextField` are customizable via properties, some internal behaviors (like error color handling and padding adjustments) are managed internally to ensure consistency with the design system.

-   **Delegation:**\
    The `TextFieldDelegate` interface is used for callbacks. Make sure to implement it properly to respond to text changes.

-   **Default Style:**\
    As for now, `style="@style/Widget.EDTS.UIKit.TextInputLayout.LabelInside"` attribute is required for TextField style to take effect.