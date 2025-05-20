# Discount Redemption Box (Tebus Murah)

## Overview

| Type               | Visual                                                      |
|--------------------|-------------------------------------------------------------|
| **Expanded State** | <img src="./assets/InfoBox/DRBoxExpanded.webp" width="360"> |
| **Shrink State**   | <img src="./assets/InfoBox/DRBoxShrink.webp" width="360">   |

| Loading                                                                                                                                                            | Scrolling                                                                                                                                            |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![Loading](https://res.cloudinary.com/dmduc9apd/image/upload/v1747651731/Screen_Recording_20250519_173328_EDTSUIKit_1-ezgif.com-video-to-gif-converter_od9qjg.gif) | ![Loading](https://res.cloudinary.com/dmduc9apd/image/upload/v1747646338/Screen_Recording_20250519_160259_EDTSUIKit_2-ezgif.com-optimize_ajfaji.gif) |

## Usage

### XML Layout

```xml
<id.co.edtslib.uikit.infobox.DiscountRedemptionBox
    android:id="@+id/discountRedemptionBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:isExpanded="false"
    android:visibility="gone"
    android:translationY="-50dp"
    app:infoText="@string/discount_redemption_box_info"
    app:layout_constraintTop_toTopOf="parent"
    tools:visibility="visible"/>
```

### Kotlin

```kotlin
binding.discountRedemptionBox.apply {
    titleText = "Tebus Murah dan Hadiah"
    infoText = "Lihat semua promo yang tersedia, yuk"
    isExpanded = true
    isLoading = false
    delegate = object : DiscountRedemptionBoxDelegate {
        override fun onClick(view: View) {
            // Handle box click
        }
    }

    attachToRecyclerView(binding.recyclerView)
}
```

## Adding Span Content
You could adjust span of the message using:
```kotlin
buildHighlightedMessage(
    context = this@GuidelinesCartActivity,
    message = "Tambah Rp50.000 untuk dapat promo",
    highlightedMessages = listOf("Rp50.000"),
    highlightedTextAppearance = listOf(
        TextStyleKey.B4_SEMIBOLD.get(context, UIKitR.color.black_70)
    )
)
```
>See more about this function inside [StringBuilderExtension](https://github.com/shidiq-uxe/edts-ui-kit/blob/main/uikit/src/main/java/id/co/edtslib/uikit/utils/StringBuilderExtension.kt)

## Public Properties

| Property     | Type            | Description                                      |
|--------------|-----------------|--------------------------------------------------|
| `titleText`  | `CharSequence?` | Title text shown when expanded.                  |
| `infoText`   | `CharSequence?` | Chip text representing discount info.            |
| `isExpanded` | `Boolean`       | Whether the box shows title and expands margins. |
| `isLoading`  | `Boolean`       | Enables shimmer and disables chip while loading. |

## Sticky Header Behavior

Attach the box to a RecyclerView so it appears only after a specific item has scrolled out of view.

```kotlin
discountBox.attachToRecyclerView(
    recyclerView = rvCart,
    targetAdapterPosition = 3 // Will show once this item scrolls past top
)
```

## Delegate Callbacks
To handle interactions, set a delegate that implements DiscountRedemptionBoxDelegate.

```kotlin
interface DiscountRedemptionBoxDelegate {
    fun onClick(view: View)
}
```

