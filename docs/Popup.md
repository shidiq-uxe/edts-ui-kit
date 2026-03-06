# PopUp

## Overview
`PopUp` is a utility object designed to simplify the creation and display of customizable alert dialogs in Android. It builds on top of the helper methods in `DialogExtension` to offer an easy-to-use API for common use cases, such as displaying confirmation or information dialogs with minimal code.

## Key Features
- **Simplified Dialog Display**: A single method for showing a dialog with comprehensive configuration options.
- **HTML Content Support**: Supports displaying rich text with HTML formatting.
- **Pre-Configured Settings**: Options for centered content, full-screen display, and button click handling.
- **Customization Hooks**: Provides lambdas for further customization of the dialog view.

## Popup Overview
| Single Button                                                                                        | Double Horizontal Button                                                                             | Double Stacked Button                                                                                | With Image                                                                                              |
|------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| ![SAB](https://res.cloudinary.com/dmduc9apd/image/upload/v1730968102/Popup/qow3f9cljfeb727pwnae.gif) | ![DHB](https://res.cloudinary.com/dmduc9apd/image/upload/v1730968101/Popup/cs07adpeu3gfr1wvtqjm.gif) | ![DSB](https://res.cloudinary.com/dmduc9apd/image/upload/v1730968102/Popup/eunbctkhwfrbltrmt66g.gif) | ![ID](https://res.cloudinary.com/dpdbzlnhr/image/upload/v1771821988/dialog_with_image_gif_2_fo8ixh.gif) |

## Method Details

### `show()`
Displays an alert dialog with flexible options for customization.

**Parameters:**
- `context` (`Context`): The context in which the dialog should be displayed.
- `isCentered` (`Boolean`): Determines if the dialog content should be center-aligned.
- `image` (`Any?`): Optional image displayed at the top of the dialog.
- `title` (`CharSequence`): The title of the dialog.
- `isTitleLarge` (`Boolean`): Determines whether the title should use a larger text appearance style.
- `displayAsHtml` (`Boolean`): Indicates if the title, message, and support message should be rendered as HTML.
- `message` (`CharSequence`): The message body of the dialog.
- `supportMessage` (`CharSequence?`): Optional secondary message displayed below the main message.
- `isFullScreen` (`Boolean`): Specifies if the dialog should be displayed in full-screen mode.
- `isActionHorizontal` (`Boolean`): Specifies if the Action Button Should be Stacked or Chained Horizontally.
- `isDismissible` (`Boolean`): Sets whether the dialog can be dismissed by outside touch or back press.
- `isCloseButtonVisible` (`Boolean`): Determines whether the close (X) button is visible.
- `positiveButton` (`String?`): The text for the positive button. If `null`, the button will not be shown.
- `negativeButton` (`String?`): The text for the negative button. If `null`, the button will not be shown.
- `onBindView` (`(ViewDialogBinding) -> Unit`): A lambda for further binding and customization of the view.
- `onPositiveButtonClick` (`(View, androidx.appcompat.app.AlertDialog) -> Unit`): Callback for handling positive button clicks.
- `onNegativeButtonClick` (`(View, androidx.appcompat.app.AlertDialog) -> Unit`): Callback for handling negative button clicks.
- `onDismiss` (`() -> Unit`): A lambda executed when the dialog is dismissed.

**Example Usage of Single Action Button:**
- You could set Button Visibility based by Action Button Text

```kotlin
PopUp.show(
    context = this,
    isCentered = true,
    title = "Important Update",
    message = "Please read the terms carefully before proceeding.",
    positiveButton = "Single Action",
    onPositiveButtonClick = { _, dialog -> dialog.dismiss() },
)
```

**Example Usage of Horizontal Chained Action Button:**
- You Could set `isActionHorizontal` to true or just leave it, since the default value is true

```kotlin
PopUp.show(
    context = this,
    isCentered = true,
    title = "Important Update",
    message = "Please read the terms carefully before proceeding.",
    positiveButton = "Accept",
    negativeButton = "Decline",
    onPositiveButtonClick = { _, dialog -> dialog.dismiss() },
    onNegativeButtonClick = { _, dialog -> dialog.dismiss() }
)
```

**Example Usage of Stacked Action Button:**
- Set `isActionHorizontal` to false

```kotlin
PopUp.show(
    context = this,
    isCentered = true,
    title = "Important Update",
    isActionHorizontal = false,
    message = "Please read the terms carefully before proceeding.",
    positiveButton = "Accept",
    negativeButton = "Decline",
    onPositiveButtonClick = { _, dialog -> dialog.dismiss() },
    onNegativeButtonClick = { _, dialog -> dialog.dismiss() }
)
```

**Example Usage of Dialog with Image:**
- Assign value to `image` parameter, it can be a Drawable or a URL string.

```kotlin
PopUp.show(
    context = this,
    image = R.drawable.ic_close, 
    title = "Payment Successful",
    message = "Your transaction has been completed successfully.",
    supportMessage = "You can view the receipt in your transaction history.",
    isCloseButtonVisible = true,
    positiveButton = "Continue",
    onPositiveButtonClick = { _, dialog ->
        dialog.dismiss()
    }
)

```

## Benefits
- Streamlined API: Reduces boilerplate code and simplifies dialog creation.
- Consistent Behavior: Leverages the underlying methods in DialogExtension to ensure consistent behavior across different dialogs.

## Related Documentation
For more advanced customization and building your own dialogs from scratch, refer to the [`Dialog`](./Dialog.md) documentation.