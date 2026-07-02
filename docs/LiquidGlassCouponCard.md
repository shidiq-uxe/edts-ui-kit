# LiquidGlassCouponCard Component

`LiquidGlassCouponCard` is a customizable Android UI component that provides a liquid glass styled card with blur effects, badge support, and interactive animations. It is designed to enhance UX by offering a modern, visually appealing coupon card experience with liquid glass aesthetics.

---

## Features

- **Customizable Title and Subtitle**: Update card title and subtitle text dynamically.
- **Badge Support**: Display badges with customizable text and visibility.
- **Custom Icons**: Set unique start and end icons for the card.
- **Glassmorphism Effect**: Includes blur background with customizable radius and overlay to create liquid glass appearance.
- **Interactive Animations**: Touch feedback with smooth scale animations.
- **Delegate Support**: Notify listeners about card interaction events (press, release, click).
- **XML Attribute Support**: Configure properties directly in XML layouts.

## Preview
![LiquidGlassCouponCard Preview](https://res.cloudinary.com/dpdbzlnhr/image/upload/c_scale,w_600/v1767947070/liquidglass-ezgif.com-optimize_hgr0jm.gif)

## Installation

Include the `LiquidGlassCouponCard` component in your layout file:

```xml
<id.co.edtslib.uikit.card.LiquidGlassCouponCard
    android:id="@+id/liquidGlassCouponCard"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:titleText="Kupon Saya"
    app:subTitleText="Kumpulkan kupon yang kamu punya"
    app:badgeText="5"
    app:badgeVisible="true"
    app:startIcon="@drawable/ic_coupon_star_24"
    app:endIcon="@drawable/ic_chevron_right_16"
    app:endIconVisible="true" />
```

* * * * *

## Usage

### 1\. Setup in Kotlin

Add the `LiquidGlassCouponCard` to your layout and configure it programmatically in your Activity or Fragment:

```kotlin
val couponCard = findViewById<LiquidGlassCouponCard>(R.id.liquidGlassCouponCard)

// Configure card content
couponCard.title = "Coupon Title"
couponCard.subtitle = "Coupon Subtitle"
couponCard.badgeText = "3"
couponCard.isBadgeVisible = true
couponCard.startIconRes = R.drawable.ic_custom_coupon
couponCard.endIconRes = R.drawable.ic_arrow_right
couponCard.isEndIconVisible = true

// Setup blur effect (required for glassmorphism)
couponCard.setupBlur(
    activity = this,
    target = binding.rootView // Your root view or target view to blur
)

// Setup delegate for interaction callbacks
couponCard.delegate = object : LiquidGlassCouponCardDelegate {
    override fun onCardPressed(card: LiquidGlassCouponCard) {
        // Handle card press event
        Log.d("CouponCard", "Card pressed")
    }

    override fun onCardReleased(card: LiquidGlassCouponCard) {
        // Handle card release event
        Log.d("CouponCard", "Card released")
    }

    override fun onCardClicked(card: LiquidGlassCouponCard) {
        // Handle card click event
        // Navigate to coupon list or perform action
        navigateToCouponList()
    }
}
```

* * * * *

### 2\. XML Attributes

You can configure title, subtitle, badge, and icons directly in XML using the following attributes:

| Attribute Name    | Description                          | Default Value                     |
|-------------------|--------------------------------------|-----------------------------------|
| `titleText`       | Title text for the card              | `"Kupon Saya"`                    |
| `subTitleText`    | Subtitle text for the card           | `"Kumpulkan kupon yang kamu punya"` |
| `badgeText`       | Text to display in the badge         | `null`                            |
| `badgeVisible`    | Toggle badge visibility              | `true`                            |
| `startIcon`       | Icon displayed on the left side      | `@drawable/ic_coupon_star_24`     |
| `endIcon`         | Icon displayed on the right side     | `@drawable/ic_chevron_right_16`   |
| `endIconVisible`  | Toggle end icon visibility           | `true`                            |

* * * * *

### 3\. Delegate

Implement the `LiquidGlassCouponCardDelegate` to listen for card interaction events:

```kotlin
interface LiquidGlassCouponCardDelegate {
    fun onCardPressed(card: LiquidGlassCouponCard)
    fun onCardReleased(card: LiquidGlassCouponCard)
    fun onCardClicked(card: LiquidGlassCouponCard)
}
```

### 4\. Setting Up Blur Effect

The glassmorphism effect requires setting up the blur effect. The `BlurTarget` should contain the content that will be blurred and must be positioned behind the card in the view hierarchy.

```xml
<eightbitlab.com.blurview.BlurTarget
    android:id="@+id/blurTarget"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Content to blur -->
    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/some_bg_image" />

</eightbitlab.com.blurview.BlurTarget>
```

Then provide the `BlurTarget` when calling `setupBlur()`:

```kotlin
// In  Activity
couponCard.setupBlur(
    activity = this,
    target = binding.rootView
)

// Or in Fragment
couponCard.setupBlur(
    activity = requireActivity(),
    target = binding.fragmentRoot
)
```

> **Important:** An empty `BlurTarget` may cause the blur effect to appear as a transparent overlay, particularly on Android API levels below 31.

For advanced configuration and implementation details, refer to the official [BlurView](https://github.com/Dimezis/BlurView) documentation.

* * * * *

## Example

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCouponCard()
    }

    private fun setupCouponCard() {
        binding.liquidGlassCouponCard.apply {
            // Customize appearance
            title = "My Vouchers"
            subtitle = "You have special offers waiting"
            badgeText = "NEW"
            startIconRes = R.drawable.ic_gift_24
            
            // Setup blur effect
            setupBlur(
                activity = this@MainActivity,
                target = binding.root
            )
            
            // Handle interactions
            delegate = object : LiquidGlassCouponCardDelegate {
                override fun onCardPressed(card: LiquidGlassCouponCard) {
                    // Optional: Add haptic feedback
                }

                override fun onCardReleased(card: LiquidGlassCouponCard) {
                    // Optional: Reset any custom press state
                }

                override fun onCardClicked(card: LiquidGlassCouponCard) {
                    // Navigate to coupon details
                    startActivity(Intent(this@MainActivity, CouponListActivity::class.java))
                }
            }
        }
    }
}
```

* * * * *

## Customization

### Programmatic Updates

You can dynamically update all properties at runtime:

```kotlin
// Update text content
couponCard.title = "Updated Title"
couponCard.subtitle = "Updated description"

// Update badge
couponCard.badgeText = "10"
couponCard.isBadgeVisible = true

// Update icons
couponCard.startIconRes = R.drawable.ic_new_icon
couponCard.endIconRes = R.drawable.ic_new_arrow
couponCard.isEndIconVisible = false

// Hide badge
couponCard.isBadgeVisible = false
```

## Animation

The component includes touch feedback animations:
- **Press animation**: Scales down to 97% over 100ms
- **Release animation**: Scales back to 100% over 100ms

* * * * *

## Visual Effects

### Glassmorphism Effect

The card features a liquid glass appearance with:
- **Blur background**: Blurs content behind the card
- **Rim edge glow**: Gradient border effect using `LinearGradient`
- **Transparent background**: Allows blur effect to show through
- **Rounded corners**: 8dp corner radius

### Gradient Rim

The rim edge uses a horizontal gradient shader with four color stops:
- Left edge: `liquidGlassRim` color (visible)
- Left-center: Transparent
- Right-center: Transparent
- Right edge: `liquidGlassRim` color (visible)

This creates a subtle glow effect on the left and right edges of the card.

* * * * *

## Requirements

- **Dependencies**:
    - BlurView 3.2.0: `implementation("com.github.Dimezis:BlurView:version-3.2.0")`

* * * * *

## Notes

- Make sure to call `setupBlur()` after the view hierarchy is ready for the liquid glass effect results