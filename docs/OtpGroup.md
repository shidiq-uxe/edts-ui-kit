# OtpGroup

Use `PoinkuOtpGroup` for Poinku's segmented entry. The default recipe shows four 36 × 46dp cells with 8dp gaps and Rubik Semibold digits. Cell appearance follows empty, focused, filled, error, and disabled states.

The OTP guide uses the shared product toolbar. Choose **Core**, **Poinku**, or **Legacy** to view the available implementations. Poinku is selected initially. Product selection and input values survive recreation.

```xml
<id.co.edtslib.uikit.recipes.otp.poinku.PoinkuOtpGroup
    android:id="@+id/ogVerification"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:otpCount="4" />
```

```kotlin
binding.ogVerification.setOnTextChangedListener { code ->
    // Update UI that depends on the current input.
}
binding.ogVerification.setOnOtpCompleteListener { code ->
    verifyCode(code)
}
```

Typing advances to the next cell. Pasting distributes the digits across remaining cells. Backspace from an empty cell moves to the previous one. Use `otp` to read or set the current digits and `clear()` to reset the cells and focus the first one. Use `isError` for the validation state and `isEnabled` to disable entry. `animateError` and `vibrateOnError` control error feedback.

For a Poinku legacy caller, migrate the XML tag and replace `OtpDelegate.setOnOtpCompleteListener` and `OtpDelegate.setOnTextChangeListener` with the two listeners above. The recipe emits one text-change event per paste or clear. Existing Klik callers can use the legacy group below.

## Legacy OtpGroup reference

The `OtpGroup` class is a custom Android UI component that extends `LinearLayoutCompat` and is used to create a series of input fields for handling one-time password (OTP) entry. This component facilitates seamless user interaction for OTP input, complete with features like automatic focus transitions, error animations, and vibrational feedback.

## Features

-   **Customizable OTP Input Fields**: Allows configuration of the number of OTP input fields and the spacing between them.
-   **Error Handling**: Provides visual and haptic feedback on input errors.
-   **Automatic Focus Transition**: Moves the focus to the next input field when text is entered and backtracks when deleted.
-   **Delegation Pattern**: Supports a delegate interface (`OtpDelegate`) for listening to OTP completion and text changes.
-   **Clear Functionality**: Clears all input fields and resets the focus to the first field.

## Properties

### Public Properties

-   **`delegate: OtpDelegate?`**
    -   An interface used to handle OTP completion and text change events.
-   **`isError: Boolean`**
    -   A flag that applies an error state to all input fields. If set to `true`, it triggers error animations and optionally vibrates the device.
-   **`shouldAnimateError: Boolean`**
    -   Controls whether error animations should be played when `isError` is set to `true`. Default is `true`.
-   **`shouldVibrate: Boolean`**
    -   Determines whether haptic feedback should occur when an error is set. Default is `true`.

### Private Properties

-   **`otpCount: Int`**
    -   The number of OTP input fields. Default is 4.
-   **`marginBetween: Int`**
    -   The space between the OTP input fields, specified in pixels. Default is 8 dp.
-   **`otpInputLayouts: MutableList<TextField>`**
    -   A list of `TextField` components used to hold the OTP inputs.

## XML Attributes

The `OtpGroup` supports the following custom attributes:

-   `otpCount`: Sets the number of OTP input fields.
-   `marginBetween`: Specifies the margin between input fields.
-   `isError`: Indicates if the input fields should be in an error state.
-   `animateError`: Enables or disables error animations.
-   `vibrateOnError`: Enables or disables vibration feedback on error.

## Methods

### `clear()`

Clears all input fields and resets the focus to the first field.

```kotlin
fun clear() {
    otpInputLayouts.mapNotNull { it.editText }.forEach { editText ->
        editText.setText("")
        otpInputLayouts[0].requestFocus()
    }
}
```

### `otp`

A read-only property that returns the combined OTP input as a `String`.

```kotlin
val otp: String get() = otpInputLayouts.mapNotNull { it.editText }.joinToString("") { it.text.toString() }
```

Event Handling
--------------

-   **Text Change Listener**: Automatically moves focus to the next field upon text entry and back to the previous field on deletion.
-   **Key Listener**: Handles backspace to move the focus to the previous field if the current field is empty.
-   **Focus Change Listener**: Ensures the cursor position is set to the end when a field gains focus.

Delegate Interface
------------------

Implement `OtpDelegate` to handle OTP completion and text change events.

### Example Usage

```kotlin
`otpGroup.delegate = object : OtpDelegate {
    override fun setOnOtpCompleteListener(otp: String) {
        // Handle OTP completion
    }

    override fun setOnTextChangeListener(text: String) {
        // Handle text change
    }
}
```