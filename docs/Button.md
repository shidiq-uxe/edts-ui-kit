# Buttons

This document outlines the button styles available in the UI Kit. Each button style is designed to be customizable and easy to implement in your Android projects.

## Button Styles Overview

| Button Style                         | Description                                                                                  | Implementation Details                               | Preview                                                                           | Disabled Preview                                                                                    | Destructive Preview                                                                                       |
|--------------------------------------|----------------------------------------------------------------------------------------------|------------------------------------------------------|-----------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **Filled Primary Button Small**      | A solid color button with text and icon color matching the theme. Ideal for primary actions. | `Widget.EDTS.UIKit.Button.Filled.Primary.Small`      | ![Filled Primary Button Small](assets/Button/filled_primary_small.webp)           | ![Filled Primary Button Small Disabled](assets/Button/filled_primary_small_disabled.webp)           | ![Filled Primary Button Small Destructive](assets/Button/filled_primary_small_destructive.webp)           | 
| **Filled Primary Button Medium**     | A medium-sized filled button, suitable for secondary actions.                                | `Widget.EDTS.UIKit.Button.Filled.Primary.Medium`     | ![Filled PrimaryButton Medium](assets/Button/filled_primary_medium.webp)          | ![Filled Primary Button Medium Disabled](assets/Button/filled_primary_medium_disabled.webp)         | ![Filled Primary Button Medium Destructive](assets/Button/filled_primary_medium_destructive.webp)         |
| **Filled Primary Button Large**      | A large-sized filled button, suitable for prominent actions.                                 | `Widget.EDTS.UIKit.Button.Filled.Primary.Large`      | ![Filled Primary Button Large](assets/Button/filled_primary_large.webp)           | ![Filled Primary Button Large Disabled](assets/Button/filled_primary_large_disabled.webp)           | ![Filled Primary Button Large Destructive](assets/Button/filled_primary_large_destructive.webp)           |
| **Outlined Secondary Button Small**  | A button with a white background and an outlined border. Ideal for secondary actions.        | `Widget.EDTS.UIKit.Button.Outlined.Secondary.Small`  | ![Outlined Secondary Button Small](assets/Button/outlined_secondary_small.webp)   | ![Outlined Secondary Button Small Disabled](assets/Button/outlined_secondary_small_disabled.webp)   | ![Outlined Secondary Button Small Destructive](assets/Button/outlined_secondary_small_destructive.webp)   |
| **Outlined Secondary Button Medium** | A medium-sized outlined button with increased padding.                                       | `Widget.EDTS.UIKit.Button.Outlined.Secondary.Medium` | ![Outlined Secondary Button Medium](assets/Button/outlined_secondary_medium.webp) | ![Outlined Secondary Button Medium Disabled](assets/Button/outlined_secondary_medium_disabled.webp) | ![Outlined Secondary Button Medium Destructive](assets/Button/outlined_secondary_medium_destructive.webp) |
| **Outlined Secondary Button Large**  | A large-sized outlined button with increased icon size.                                      | `Widget.EDTS.UIKit.Button.Outlined.Secondary.Large`  | ![Outlined Secondary Button Large](assets/Button/outlined_secondary_large.webp)   | ![Outlined Secondary Button Large Disabled](assets/Button/outlined_secondary_large_disabled.webp)   | ![Outlined Secondary Button Large Destructive](assets/Button/outlined_secondary_large_destructive.webp)   |
| **Outlined Tertiary Button Small**   | A variant of the outlined button with a different stroke color. Ideal for tertiary actions.  | `Widget.EDTS.UIKit.Button.Outlined.Tertiary.Small`   | ![Outlined Button Tertiary Small](assets/Button/outlined_tertiary_small.webp)     | ![Outlined Tertiary Button Small Disabled](assets/Button/outlined_tertiary_small_disabled.webp)     | ![Outlined Tertiary Button Small Destructive](assets/Button/outlined_tertiary_small_destructive.webp)     |
| **Outlined Tertiary Button Medium**  | A medium-sized tertiaty button with increased padding.                                       | `Widget.EDTS.UIKit.Button.Outlined.Tertiary.Small`   | ![Outlined Button Tertiary Medium](assets/Button/outlined_tertiary_medium.webp)   | ![Outlined Tertiary Button Medium Disabled](assets/Button/outlined_tertiary_medium_disabled.webp)   | ![Outlined Tertiary Button Medium Destructive](assets/Button/outlined_tertiary_medium_destructive.webp)   |
| **Outlined Tertiary Button Large**   | A large-sized tertiaty button with increased padding.                                        | `Widget.EDTS.UIKit.Button.Outlined.Tertiary.Small`   | ![Outlined Button Tertiary Large](assets/Button/outlined_tertiary_large.webp)     | ![Outlined Tertiary Button Large Disabled](assets/Button/outlined_tertiary_large_disabled.webp)     | ![Outlined Tertiary Button Large Destructive](assets/Button/outlined_tertiary_large_destructive.webp)     |
| **Text Button Small**                | A text-only button with no background, suitable for less prominent actions.                  | `Widget.EDTS.UIKit.Button.TextButton.Small`          | ![Text Button](assets/Button/text_button_small.webp)                              | ![Text Button Disabled](assets/Button/text_button_small_disabled.webp)                              | ![Filled Button Medium Destructive](assets/Button/text_button_small_destructive.webp)                     |
| **Text Button Medium**               | A medium-sized text button with increased text size.                                         | `Widget.EDTS.UIKit.Button.TextButton.Medium`         | ![Medium Text Button](assets/Button/text_button_medium.webp)                      | ![Text Button Medium Disabled](assets/Button/text_button_medium_disabled.webp)                      | ![Filled Button Medium Destructive](assets/Button/text_button_medium_destructive.webp)                    |
| **Text Button Large**                | A large-sized text button with increased padding.                                            | `Widget.EDTS.UIKit.Button.TextButton.Large`          | ![Large Text Button](assets/Button/text_button_large.webp)                        | ![Text Button Large Disabled](assets/Button/text_button_large_disabled.webp)                        | ![Filled Button Large Destructive](assets/Button/text_button_large_destructive.webp)                      |

**Implementation**:
```xml
<id.co.edtslib.uikit.button.Button
    android:id="@+id/btnFilled"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/s"
    android:layout_marginHorizontal="@dimen/s"
    android:text="Filled Button"
    app:buttonType="filled"/>
```
Or
```xml
<Button
    android:id="@+id/btnFilled"
    style="@style/Widget.EDTS.UIKit.Button.Filled"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="@dimen/s"
    android:layout_marginHorizontal="@dimen/s"
    android:text="Filled Button"/>
```

### Note on Setting Styles via XML vs. Runtime

1. **Inflation Efficiency**: When styles are applied through XML, they are processed during the inflation stage, leading to a more efficient layout setup. The attributes are parsed and set before the view is fully initialized, ensuring the layout is optimized from the start.
2. **Runtime Overhead**: Applying styles at runtime requires the view to be updated after its initial creation. This means additional processing is needed to override attributes, potentially leading to performance impacts and additional redraws.

**Recommendation**: Prefer defining view styles via XML where possible to ensure better performance and a smoother user experience.

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
| **Variant Button**          | ![Variant](https://res.cloudinary.com/dmduc9apd/image/upload/v1730965401/Button/xrh1wemdtzzc9dcusn6g.gif) |

1.  The touch animation is already integrated and works out-of-the-box, providing scale feedback when the button is pressed and released.

## Properties:

-   **`shouldShowShimmer`**: A `Boolean` property to enable or disable the shimmer effect.
-   **`shimmerFrameLayout`**: An optional `ShimmerFrameLayout` instance that manages the shimmer effect when `shouldShowShimmer` is `true`.
-   **`buttonType`**: An Enum Options that manages the Button Styles.
-   **`pressedScale`**: A `Float` that determines hover effect down scale.
-   **`isDestructive`**: A `Boolean` that toggles the destructive visual state of the button (e.g., red/danger styling). Triggers a drawable state refresh to apply the correct color selectors.

## Usage
1.  **Include the `Button` class in your project.**
2.  Set `shouldShowShimmer` to `true` to activate the shimmer effect.
3. Use `pressedScale` from `0` to `1.0` to adjust hover effect.
4. Set `isDestructive` to `true` to trigger destructive visual state.

```xml
<id.co.edtslib.uikit.button.Button
    app:shouldShowShimmer="true"
    app:pressedScale="0.9"
    app:isDestructive="true"
/>
```
Or
```kotlin
edtsButton.shouldShowShimmer = true
edtsButton.pressedScale = 0.9f
edtsButton.isDestructive = true
```

## Dependencies

Ensure that your project includes the necessary dependencies for `MaterialButton` and `ShimmerFrameLayout`.

```gradle
implementation 'com.google.android.material:material:1.8.0'
implementation 'com.facebook.shimmer:shimmer:0.5.0'
```