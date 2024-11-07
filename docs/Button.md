# Buttons

This document outlines the button styles available in the UI Kit. Each button style is designed to be customizable and easy to implement in your Android projects.

## Button Styles Overview

| Button Style                  | Description                                                                                             | Implementation Details                        | Preview                                                           | Disabled Preview                                                   |
|-------------------------------|---------------------------------------------------------------------------------------------------------|-----------------------------------------------|-------------------------------------------------------------------|--------------------------------------------------------------------|
| **Filled Button**             | A solid color button with text and icon color matching the theme. Ideal for primary actions.            | `Widget.EDTS.UIKit.Button.Filled`             | ![Filled Button](assets/Button/filled.jpeg)                       | ![Filled Button Disabled](assets/Button/disabled.jpeg)             |
| **Filled Button Medium**      | A medium-sized filled button, suitable for secondary actions.                                           | `Widget.EDTS.UIKit.Button.Filled.Medium`      | ![Filled Button Medium](assets/Button/filled_medium.jpeg)         | ![Filled Button Medium Disabled](assets/Button/disabled.jpeg)      |
| **Filled Button Variant**     | A variant of the filled button with an alternative background color.                                    | `Widget.EDTS.UIKit.Button.Filled.Variant`     | ![Filled Button Variant](assets/Button/variant.jpeg)              | ![Filled Button Variant Disabled](assets/Button/disabled.jpeg)     |
| **Outlined Button**           | A button with a transparent background and an outlined border. Ideal for secondary or tertiary actions. | `Widget.EDTS.UIKit.Button.Outlined`           | ![Outlined Button](assets/Button/outline_secondary.jpg)           | ![Outlined Button Disabled](assets/Button/disabled.jpeg)           |
| **Outlined Button Medium**    | A medium-sized outlined button with increased padding.                                                  | `Widget.EDTS.UIKit.Button.Outlined.Medium`    | ![Outlined Button Medium](assets/Button/secondary_medium.jpeg)    | ![Outlined Button Medium Disabled](assets/Button/disabled.jpeg)    |
| **Outlined Button Secondary** | A secondary variant of the outlined button with a different stroke color.                               | `Widget.EDTS.UIKit.Button.Outlined.Secondary` | ![Outlined Button Secondary](assets/Button/secondary_medium.jpeg) | ![Outlined Button Secondary Disabled](assets/Button/disabled.jpeg) |
| **Text Button**               | A text-only button with no background, suitable for less prominent actions.                             | `Widget.EDTS.UIKit.Button.TextButton`         | ```N/A```                                                         | ![Text Button Disabled](assets/Button/disabled.jpeg)               |
| **Text Button Medium**        | A medium-sized text button with added padding.                                                          | `Widget.EDTS.UIKit.Button.TextButton.Medium`  | ```N/A```                                                         | ![Text Button Medium Disabled](assets/Button/disabled.jpeg)        |
| **Icon Button**               | A button designed primarily for icons with no text.                                                     | `Widget.EDTS.UIKit.Button.IconButton`         | ```N/A```                                                         | ![Icon Button Disabled](assets/Button/disabled.jpeg)               |

**Implementation**:
```xml
<style name="Widget.EDTS.UIKit.Button.Filled" parent="Widget.Material3.Button.UnelevatedButton">
    <!-- Styles here -->
</style>
```

# Custom Button with Pulsing Shimmer Effect and Touch Animation

This repository provides an implementation of a custom `Button` class for Android that extends `MaterialButton` and includes features such as an optional pulsing shimmer effect and touch animation feedback.

## Features

-   **Shimmer Effect**: Adds a pulsing shimmer effect when `shouldShowShimmer` is set to `true`, making the button visually engaging.
-   **Touch Animation**: Includes scaling animation to provide tactile feedback when the button is pressed and released.
-   **Configurable**: The shimmer effect can be easily enabled or disabled with the `shouldShowShimmer` property.
-   **Styling**: You could change the style of the button based on [Button Styles](#button-styles-overview) with xml style attribute.

## Button Overview

| Button Style                | Description                                                                                               |
|-----------------------------|-----------------------------------------------------------------------------------------------------------|
| **(Default) Filled Button** | ![Filled](https://res.cloudinary.com/dmduc9apd/image/upload/v1730965402/Button/ydpmcew44h7dwwyp9cws.gif)  |
| **Secondary Button**        | ![Variant](https://res.cloudinary.com/dmduc9apd/image/upload/v1730965401/Button/xrh1wemdtzzc9dcusn6g.gif) |

## Usage
1.  **Include the `Button` class in your project.**
2.  Set `shouldShowShimmer` to `true` to activate the shimmer effect.

```kotlin
val customButton = findViewById<Button>(R.id.custom_button)
customButton.shouldShowShimmer = true
```

1.  The touch animation is already integrated and works out-of-the-box, providing scale feedback when the button is pressed and released.

## Properties:

-   **`shouldShowShimmer`**: A `Boolean` property to enable or disable the shimmer effect.
-   **`shimmerFrameLayout`**: An optional `ShimmerFrameLayout` instance that manages the shimmer effect when `shouldShowShimmer` is `true`.

## Dependencies

Ensure that your project includes the necessary dependencies for `MaterialButton` and `ShimmerFrameLayout`.

```gradle
implementation 'com.google.android.material:material:your_version'
implementation 'com.facebook.shimmer:shimmer:your_version'
```
