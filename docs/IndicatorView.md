# IndicatorView

The `IndicatorView` is a customizable indicator component designed to work seamlessly with ViewPager2. It provides a way to visually indicate the current page or item in a ViewPager2 and offers various customization options for appearance and behavior.

## Features

- Supports multiple indicator styles (e.g., Circle, Dash, and more)
- Customizable size, color, and spacing
- Supports both horizontal and vertical orientations
- Smooth animations for indicator transitions
- Easy integration with ViewPager2

## Getting Started

### 1. Adding the IndicatorView to your layout

To use the `IndicatorView`, simply add it to your layout XML file and link it with your ViewPager2.

```xml
<id.co.edtslib.uikit.indicator.IndicatorView
    android:id="@+id/indicator_view"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:indicator_checked_color="@color/primaryColor"
    app:indicator_default_color="@color/secondaryColor"
    app:indicator_default_width="8dp"
    app:indicator_selected_width="10dp"
    app:indicator_slider_height="4dp"
    app:indicator_gap="4dp"
    app:rtl="false"
    app:indicator_orientation="horizontal"
    app:indicator_slide_mode="normal"
    app:indicator_style="circle"
/>
```

### 2. Linking the IndicatorView with ViewPager2
In your activity or fragment, set up the IndicatorView to work with your ViewPager2.

```kotlin
val viewPager: ViewPager2 = findViewById(R.id.view_pager) 
val indicatorView: IndicatorView = findViewById(R.id.indicator_view) 
indicatorView.setupWithViewPager(viewPager)
```

Customization
Attributes
You can customize the IndicatorView through the following XML attributes:

## `IndicatorView` Methods and Properties

## Methods

| Method Name                                                                                       | Description                                              | Parameters                                               | Returns             |
|---------------------------------------------------------------------------------------------------|----------------------------------------------------------|----------------------------------------------------------|---------------------|
| `getNormalSlideWidth()`                                                                           | Gets the width of the normal slider.                     | -                                                        | `Float`             |
| `setNormalSlideWidth(normalSliderWidth: Float)`                                                   | Sets the width of the normal slider.                     | `normalSliderWidth: Float`                               | `Unit`              |
| `getCheckedSlideWidth()`                                                                          | Gets the width of the checked slider.                    | -                                                        | `Float`             |
| `setCheckedSlideWidth(@Px checkedSliderWidth: Float)`                                             | Sets the width of the checked slider.                    | `checkedSliderWidth: Float`                              | `Unit`              |
| `checkedSliderWidth`                                                                              | Gets the width of the checked slider.                    | -                                                        | `Float`             |
| `setCurrentPosition(currentPosition: Int)`                                                        | Sets the current position of the indicator.              | `currentPosition: Int`                                   | `Unit`              |
| `getCurrentPosition()`                                                                            | Gets the current position of the indicator.              | -                                                        | `Int`               |
| `getIndicatorGap()`                                                                               | Gets the gap between indicators.                         | -                                                        | `Float`             |
| `setIndicatorGap(indicatorGap: Float)`                                                            | Sets the gap between indicators.                         | `indicatorGap: Float`                                    | `Unit`              |
| `setCheckedColor(@ColorInt normalColor: Int)`                                                     | Sets the color of the checked indicator.                 | `normalColor: Int`                                       | `Unit`              |
| `getCheckedColor()`                                                                               | Gets the color of the checked indicator.                 | -                                                        | `Int`               |
| `setNormalColor(@ColorInt normalColor: Int)`                                                      | Sets the color of the normal indicator.                  | `normalColor: Int`                                       | `Unit`              |
| `getSlideProgress()`                                                                              | Gets the slide progress of the indicator.                | -                                                        | `Float`             |
| `setSlideProgress(slideProgress: Float)`                                                          | Sets the slide progress of the indicator.                | `slideProgress: Float`                                   | `Unit`              |
| `getPageSize()`                                                                                   | Gets the number of pages for the indicator.              | -                                                        | `Int`               |
| `setPageSize(pageSize: Int): BaseIndicatorView`                                                   | Sets the number of pages for the indicator.              | `pageSize: Int`                                          | `BaseIndicatorView` |
| `setSliderColor(@ColorInt normalColor: Int, @ColorInt selectedColor: Int): BaseIndicatorView`     | Sets the colors for normal and selected sliders.         | `normalColor: Int`, `selectedColor: Int`                 | `BaseIndicatorView` |
| `setSliderWidth(@Px sliderWidth: Float): BaseIndicatorView`                                       | Sets the width of the slider.                            | `sliderWidth: Float`                                     | `BaseIndicatorView` |
| `setSliderWidth(@Px normalSliderWidth: Float, @Px selectedSliderWidth: Float): BaseIndicatorView` | Sets the widths of normal and selected sliders.          | `normalSliderWidth: Float`, `selectedSliderWidth: Float` | `BaseIndicatorView` |
| `setSliderGap(sliderGap: Float): BaseIndicatorView`                                               | Sets the gap between sliders.                            | `sliderGap: Float`                                       | `BaseIndicatorView` |
| `getSlideMode()`                                                                                  | Gets the current slide mode.                             | -                                                        | `Int`               |
| `setSlideMode(@IndicatorSlideModeAnnotation slideMode: Int): BaseIndicatorView`                   | Sets the slide mode of the indicator.                    | `slideMode: Int`                                         | `BaseIndicatorView` |
| `setIndicatorStyle(@IndicatorStyleAnnotation indicatorStyle: Int): BaseIndicatorView`             | Sets the style of the indicator.                         | `indicatorStyle: Int`                                    | `BaseIndicatorView` |
| `setSliderHeight(sliderHeight: Float): BaseIndicatorView`                                         | Sets the height of the slider.                           | `sliderHeight: Float`                                    | `BaseIndicatorView` |
| `setupWithViewPager(viewPager2: ViewPager2)`                                                      | Sets up the indicator with a ViewPager2.                 | `viewPager2: ViewPager2`                                 | `Unit`              |
| `showIndicatorWhenOneItem(showIndicatorWhenOneItem: Boolean)`                                     | Shows or hides the indicator when there's only one item. | `showIndicatorWhenOneItem: Boolean`                      | `Unit`              |
| `setIndicatorOptions(options: IndicatorOptions)`                                                  | Sets the indicator options.                              | `options: IndicatorOptions`                              | `Unit`              |

## Properties

| Property Name        | Description                           | Returns |
|----------------------|---------------------------------------|---------|
| `checkedSliderWidth` | Gets the width of the checked slider. | `Float` |


Example Usage
Below is an example of using IndicatorView in an application:
```xml
<id.co.edtslib.uikit.indicator.IndicatorView
    android:id="@+id/indicator_view"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:indicator_checked_color="@color/primaryColor"
    app:indicator_default_color="@color/secondaryColor"
    app:indicator_default_width="8dp"
    app:indicator_selected_width="10dp"
    app:indicator_slider_height="4dp"
    app:indicator_gap="4dp"
    app:rtl="false"
    app:indicator_orientation="horizontal"
    app:indicator_slide_mode="normal"
    app:indicator_style="circle"
/>
    
<androidx.viewpager2.widget.ViewPager2
    android:id="@+id/view_pager"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```
```kotlin
    val viewPager: ViewPager2 = findViewById(R.id.view_pager)
    val indicatorView: IndicatorView = findViewById(R.id.indicator_view)
    indicatorView.setupWithViewPager(viewPager)
```

| Attrs  | Circle                                                                                                                                                                                                                                                                                           | Dash                                                                                                                                                                                                                                                                                       | Round Rect                                                                                                                                                                                                                                                                                                   |
|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NORMAL | ![Normal_Circle](https://camo.githubusercontent.com/f41aa67c7b8601ae5d62532e229a2f926ebea493e4b4f0ce3fed74fbbd3303a8/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f736c6964655f636972636c655f6e6f726d616c2e676966) | ![Dash Normal](https://camo.githubusercontent.com/978237c449fa1a16a1ac99bc3f7130c66e4e29ba5ef500faf820ff52d79de0bf/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f646173685f6e6f726d616c2e676966) | ![Round Rect Normal](https://camo.githubusercontent.com/9d0af0551d77e29710f56cc5d0399fdfe3177364e1c2c092d4be13af6ed3ab2b/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f726f756e645f726563745f6e6f726d616c2e676966) |
| SMOOTH | ![Smooth Circle](https://camo.githubusercontent.com/2848499b9e9e63b5afb6887f3183208cbf5229d146bbebbf87d0cf4ed8970e7d/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f736c6964655f636972636c655f736d6f6f74682e676966) | ![Dash Smooth](https://camo.githubusercontent.com/1f67c990fea0696618049d9943a56915127261b898a694ba69e9fbcaa682d8b7/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f646173685f736d6f6f74682e676966) | ![Round Rect Smooth](https://camo.githubusercontent.com/567f9db6caeb2fb4b860ce31d892dce0c45f33f2293764bdf768e38a4a351e5a/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f726f756e645f726563745f736d6f6f74682e676966) | 
| WORM   | ![Worm Circle](https://camo.githubusercontent.com/da921ea081f47f5c27c218a32c6a0e4159206fe227f8e1b847fface7a8ec0fdd/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f736c6964655f636972636c655f776f726d2e676966)       | ![Dash Worm](https://camo.githubusercontent.com/422d75eef07f7745d1a3f98f2962e4d98ebe8813a43e44f45594c5a8519214e6/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f646173685f776f726d2e676966)       | ![Round Rect Worm](https://camo.githubusercontent.com/5fee2b666691a8f3cffbb8b004586991002178ef0c0d365daad58a9a30d12804/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f726f756e645f726563745f776f726d2e676966)       | 
| COLOR  | ![Color Circle](https://camo.githubusercontent.com/4d55a8187e85dd937d7950e412fbfc056e1e5e78eb5255da51f90cb95e128bbb/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f736c6964655f636972636c655f636f6c6f722e676966)    | ![Dash Color](https://camo.githubusercontent.com/82ff51f8b07fccb88186b5c265343917db65b17198ec7b358a0e9efac064006e/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f646173685f636f6c6f722e676966)    | ![Round Rect Color](https://camo.githubusercontent.com/a3c29dc81b6b7f1c9bcc6cb0ca6d580eb2b47d31171233f3fb5405cab48ed08f/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f726f756e645f726563745f636f6c6f722e676966)    | 
| SCALE  | ![Scale Circle](https://camo.githubusercontent.com/b223e194df20bdd6210ba4e5fcc4c0685db39dc76a5bff47b749893df306fa36/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f736c6964655f636972636c655f7363616c652e676966)    | ![Dash Scale](https://camo.githubusercontent.com/0ecedcd3cda77a887640dcc9692ec8128bddbb023c2dfb4c5792b219b30d5bbd/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f646173685f7363616c652e676966)    | ![Round Rect Scale](https://camo.githubusercontent.com/e7e98fe6cc3f12b5c83446cc1466ed70d0f585eaadb0c12a57bfdbd421215a89/68747470733a2f2f63646e2e6a7364656c6976722e6e65742f67682f7a6870616e7669702f696d616765732f70726f6a6563742f696e64696361746f722f7374796c655f726f756e645f726563745f7363616c652e676966)    | 

# IndicatorView Attributes

## XML Attributes

| Attribute                  | Description                                                   | Format      | Values/Options  |
|----------------------------|---------------------------------------------------------------|-------------|-----------------|
| `indicator_checked_color`  | The color of the indicator when checked.                      | `color`     | -               |
| `indicator_default_color`  | The default color of the indicator.                           | `color`     | -               |
| `indicator_default_width`  | The width of the indicator when not selected.                 | `dimension` | -               |
| `indicator_selected_width` | The width of the indicator when selected.                     | `dimension` | -               |
| `indicator_slider_height`  | The height of the slider indicator.                           | `dimension` | -               |
| `indicator_gap`            | The gap between indicators.                                   | `dimension` | -               |
| `rtl`                      | Specifies if the layout is for right-to-left (RTL) languages. | `boolean`   | `true`, `false` |

### Enumerated Attributes

| Attribute               | Description                                   | Format | Values/Options                                                   |
|-------------------------|-----------------------------------------------|--------|------------------------------------------------------------------|
| `indicator_orientation` | The orientation of the indicator layout.      | `enum` | `horizontal` (0), `vertical` (1), `rtl` (3)                      |
| `indicator_slide_mode`  | The animation mode for indicator transitions. | `enum` | `normal` (0), `smooth` (2), `worm` (3), `scale` (4), `color` (5) |
| `indicator_style`       | The style of the indicator.                   | `enum` | `circle` (0), `dash` (2), `round_rect` (4)                       |


License
This library is inspired by zhpanvip's viewpagerindicator. All credit for the inspiration goes to them. This custom implementation provides additional features and customization tailored to specific needs.