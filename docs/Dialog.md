# Dialog

## Overview
`DialogExtension.kt` is a utility class containing extension functions for creating and customizing `AlertDialog` instances in Android applications. It leverages `MaterialAlertDialogBuilder` to provide a flexible, modular, and highly configurable way to create dialogs with minimal code.

## Key Features
- **Customizable Dialogs**: Build dialogs with different configurations including title, message, custom views, and various dialog styles.
- **Centering Support**: Option to create centered dialogs for better visual appeal.
- **List-based Dialogs**: Supports different dialog types including simple lists, single-choice, and multi-choice selections.
- **Event Handling**: Integrates callback functions for handling positive button clicks and item selection in lists.

## Detailed Explanation of Functions

### `dialogBuilder()`
Creates and returns a `MaterialAlertDialogBuilder` instance with various customization options.

**Parameters:**
- `title` (`String?`): The title of the dialog. Pass `null` for no title.
- `message` (`String?`): The message body of the dialog. Pass `null` for no message.
- `icon` (`Drawable?`): An optional icon for the dialog.
- `centered` (`Boolean`): Determines whether the dialog is centered on the screen.
- `isCancelable` (`Boolean`): Sets whether the dialog can be dismissed by clicking outside or pressing the back button.
- `style` (`DialogStyle`): The type of dialog (e.g., `DEFAULT`, `SIMPLE`, `SINGLE`, `MULTI`).
- `items` (`Array<String>`): An array of strings to display in list-style dialogs.
- `view` (`((MaterialAlertDialogBuilder) -> View)?`): A lambda returning a custom view for the dialog.
- `positiveMessage` (`String?`): The text for the positive button. If `null`, no button is shown.
- `onClickedAction` (`((dialog: DialogInterface, position: Int) -> Unit)?`): Callback for handling button or item click actions.
- `onMultiChoiceAction` (`((dialog: DialogInterface, position: Int, checked: Boolean) -> Unit)?`): Callback for multi-choice item selections.

**Return Value:**
- A configured `MaterialAlertDialogBuilder` instance that can be shown or further customized.

**Example Usage:**

```kotlin
val dialog = context.dialogBuilder(
    title = "Confirmation",
    message = "Are you sure you want to proceed?",
    positiveMessage = "Yes",
    onClickedAction = { dialog, _ -> dialog.dismiss() }
).show()
```

### `alertDialog()`

Creates an `AlertDialog` instance and configures it with a custom view and additional options.

**Parameters:**

-   `view` (`View`): The custom view to be used in the dialog.
-   `isCancelable` (`Boolean`): Sets whether the dialog can be dismissed by outside touch or back press.
-   `fullScreen` (`Boolean`): Specifies if the dialog should be displayed as full-screen.
-   `configureView` (`(view: View, dialog: AlertDialog) -> Unit`): A lambda function to apply further configurations to the view and dialog.

**Return Value:**

-   A configured `AlertDialog` instance ready to be displayed.

**Example Usage:**

```kotlin
context.alertDialog(
    view = customView,
    isCancelable = false,
    fullScreen = true
) { view, dialog ->
    // Additional configuration code
    view.findViewById<Button>(R.id.confirmButton).setOnClickListener {
        dialog.dismiss()
    }
}.show()
```

Dialog Style Options
--------------------

`DialogStyle` enum supports:

-   `DEFAULT`: Basic dialog.
-   `SIMPLE`: Dialog with a simple list of items.
-   `SINGLE`: Single-choice dialog.
-   `MULTI`: Multi-choice dialog.

Related Documentation
---------------------

See the [`PopUp`](./Popup.md) class for a simplified approach to displaying customizable alert dialogs.