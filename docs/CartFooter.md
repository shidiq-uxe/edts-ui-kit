
| Visual                                                                                                                             | Description                                                                 |
|------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| <img src="https://res.cloudinary.com/dmduc9apd/image/upload/v1756885083/Screenshot_2025-09-03_at_14.36.12_gpaiep.png" width="360"> | **Expanded State** <br> `isInfoSectionVisible = true // true by default`    |
| <img src="https://res.cloudinary.com/dmduc9apd/image/upload/v1756885231/Screenshot_2025-09-03_at_14.40.05_zbdy7j.png" width="360"> | **Shrink State** <br> `isInfoSectionVisible = false`                        |
| <img src="https://res.cloudinary.com/dmduc9apd/image/upload/v1756885428/Screenshot_2025-09-03_at_14.43.34_szoyxo.png" width="360"> | **Scrolling State** <br> `attachToRecyclerView(recyclerView)`               |
| <img src="https://res.cloudinary.com/dmduc9apd/image/upload/v1758009579/7148333e-1d83-430f-a0f7-8233cf5fd713.png" width="360">     | **Coupon Can't Be Applied State** <br> `setState(CartFooter.State.WARNING)` |


| Loading                                                                                                 | Scrolling                                                                                                   | Lottie                                                                                                |
|---------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| ![Loading](https://res.cloudinary.com/dmduc9apd/image/upload/v1756889572/CartFooter_Loading_utovvs.gif) | ![Scrolling](https://res.cloudinary.com/dmduc9apd/image/upload/v1756889573/CartFooter_Scrolling_dugod7.gif) | ![Lottie](https://res.cloudinary.com/dmduc9apd/image/upload/v1756889572/CartFooter_Lottie_piemdr.gif) |


## Usage

### XML Layout

```xml
<id.co.edtslib.uikit.footer.CartFooter
    android:id="@+id/cartFooter"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:infoText="Cek promo atau tukar kupon di sini"
    app:buttonText="Beli"
    app:isExpanded="true" />
```

### Kotlin Implementation

```kotlin
binding.cartFooter.apply {
    // Basic content setup
    totalText = "Rp120.000"
    infoText = "Cek promo atau tukar kupon di sini"
    buttonText = "Beli"
    
    // Visibility controls
    isInfoSectionVisible = true
    isCashbackBadgeVisible = true
    isDiscountBadgeVisible = true
    
    // State management
    isLoading = false
    isButtonEnabled = true
    isCouponSectionExpanded = true
    // Coupon can't be applied
    setState(CartFooter.State.WARNING) // or Default
    
    // Cashback configuration
    cashbackBadgeText = "Berpotensi mendapat Poin Cash / Poin Loyalty / Stamp"
    cashbackBadgeIcon = context.drawable(R.drawable.ill_point_n_stars_24)
    
    // Discount badge
    discountBadgeText = "Hemat 20%"
    
    // Custom gradient colors for coupon section
    gradientColors = intArrayOf(
        context.color(R.color.white),
        context.color(R.color.support_gradient)
    )
    
    // Set up callbacks
    delegate = object : CartFooterDelegate {
        override fun onActionButtonClick() {
            // Handle checkout action
            showCheckoutScreen()
        }
        
        override fun onCouponSectionClick() {
            // Show coupon selection
            showCouponBottomSheet()
        }
        
        override fun onSummaryClick() {
            // Expand order summary
            toggleOrderSummary()
        }
    }
    
    // Attach to RecyclerView for sticky behavior
    attachToRecyclerView(binding.recyclerView)
}
```

## Public Properties

### Content Properties
| Property            | Type            | Description                                                                                | Default |
|---------------------|-----------------|--------------------------------------------------------------------------------------------|---------|
| `infoText`          | `CharSequence?` | Text displayed in the coupon/info section. Supports HTML rendering with custom font styles | `null`  |
| `totalText`         | `CharSequence?` | Total price text with expandable chevron icon                                              | `null`  |
| `buttonText`        | `CharSequence?` | Main action button text                                                                    | `null`  |
| `discountBadgeText` | `CharSequence?` | Text for the discount badge                                                                | `null`  |
| `cashbackBadgeText` | `CharSequence?` | Cashback information text. Supports HTML when `isHtml` is enabled                          | `null`  |

### Visibility Controls
| Property                      | Type      | Description                                                                           | Default |
|-------------------------------|-----------|---------------------------------------------------------------------------------------|---------|
| `isInfoSectionVisible`        | `Boolean` | Controls visibility of the entire info section (Price, Button, etc)                   | `true`  |
| `isCouponSectionExpanded`     | `Boolean` | Controls expansion state of coupon section                                            | `true`  |
| `isDiscountBadgeVisible`      | `Boolean` | Shows/hides the discount badge                                                        | `false` |
| `isCashbackBadgeVisible`      | `Boolean` | Shows/hides the cashback, loyalty, & stamps badge                                     | `false` |
| `isConfettiBackgroundVisible` | `Boolean` | Controls confetti background image/placeholder visibility with smooth fade transition | `true`  |

### State Management
| Property          | Type               | Description                                             | Default                    |
|-------------------|--------------------|---------------------------------------------------------|----------------------------|
| `isLoading`       | `Boolean`          | Enables shimmer loading state and disables interactions | `false`                    |
| `isButtonEnabled` | `Boolean`          | Controls button enabled/disabled state                  | `false`                    |
| `isHtml`          | `Boolean`          | Enables HTML parsing for cashback badge text            | `false`                    |
| `setState()`      | `CartFooter.State` | Controls Coupon Section default/warning state           | `CartFooter.State.Default` |

### Customization Properties
| Property            | Type       | Description                                            | Default                     |
|---------------------|------------|--------------------------------------------------------|-----------------------------|
| `gradientColors`    | `IntArray` | Array of colors for coupin section background gradient | `[white, support_gradient]` |
| `cashbackBadgeIcon` | `Drawable` | Icon for the cashback badge                            | `ill_point_n_stars_24`      |
| `animationRawRes`   | `Int`      | Raw resource for Lottie animation                      | `applied_coupon_confetti`   |

### Delegate
| Property   | Type                  | Description                                       |
|------------|-----------------------|---------------------------------------------------|
| `delegate` | `CartFooterDelegate?` | Callback interface for handling user interactions |

## Advanced Features

### HTML Text Rendering

The `infoText` property supports HTML rendering with custom font styles:

```kotlin
cartFooter.infoText = "Apply coupon for <b>extra savings</b>!"
```

### Confetti Animation

Play preloaded confetti animation when coupon is applied:

```kotlin
// Trigger confetti animation
cartFooter.playPreLoadedAnimations()
```

### Custom Gradient Background

```kotlin
cartFooter.setGradientBackground(
    colors = intArrayOf(Color.WHITE, Color.BLUE),
    orientation = GradientDrawable.Orientation.TOP_BOTTOM
)
```

### Loading State Management

```kotlin
// Show loading state
cartFooter.isLoading = true

// After API call completion
cartFooter.apply {
    isLoading = false
    totalText = "Rp150.000"
}
```

## Sticky Footer Behavior

### Basic Attachment
```kotlin
cartFooter.attachToRecyclerView(recyclerView)
```

### Without Automatic Padding
```kotlin
cartFooter.attachToRecyclerView(recyclerView, includePadding = false)
```

When `includePadding` is `true` (default), the component automatically:
- Sets `clipToPadding = false` on the RecyclerView
- Adds bottom padding equal to the footer height
- Prevents content overlap

## Delegate Interface

Implement `CartFooterDelegate` to handle user interactions:

```kotlin
interface CartFooterDelegate {
    fun onActionButtonClick()    // Main button (checkout/buy) clicked
    fun onCouponSectionClick()   // Coupon/info section clicked
    fun onSummaryClick()         // Total/summary section clicked
}
```

### Example Implementation

```kotlin
class CheckoutActivity : AppCompatActivity(), CartFooterDelegate {
    
    override fun onActionButtonClick() {
        if (validateCart()) {
            proceedToPayment()
        } else {
            showValidationErrors()
        }
    }
    
    override fun onCouponSectionClick() {
        CouponBottomSheet().show(supportFragmentManager, "coupon")
    }
    
    override fun onSummaryClick() {
        togglePriceBreakdown()
    }
}
```

## XML Attributes

The component supports the following XML attributes:

| Attribute        | Format    | Description             |
|------------------|-----------|-------------------------|
| `app:infoText`   | `string`  | Initial info text       |
| `app:buttonText` | `string`  | Initial button text     |
| `app:isExpanded` | `boolean` | Initial expansion state |