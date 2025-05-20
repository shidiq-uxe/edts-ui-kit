# Discount Redemption Chip Group 

## Overview

| State              | Visual                                                               |
|--------------------|----------------------------------------------------------------------|
| **Default State**  | <img src="./assets/ChipGroup/DR_chipGroup_default.webp" width="80">  |
| **Inactive State** | <img src="./assets/ChipGroup/DR_chipGroup_inactive.webp" width="80"> |
| **Checked State**  | <img src="./assets/ChipGroup/DR_chipGroup_checked.webp" width="80">  |

| Snapping Animation                                                                                                                                  |
|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| ![Snapping](https://res.cloudinary.com/dmduc9apd/image/upload/v1747650501/Screen_Recording_20250519_172207_EDTSUIKit-ezgif.com-optimize_osnw6h.gif) |

## Usage

### XML Layout

```xml
<id.co.edtslib.uikit.chipgroup.DiscountRedemptionChipGroup
    android:id="@+id/chipGroup"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingTop="@dimen/xxs"
    android:paddingHorizontal="@dimen/s"
    app:layout_constraintTop_toTopOf="parent"
    tools:listitem="@layout/view_discount_redemption_category"/>
```

### Kotlin

```kotlin
binding.chipGroup.items = List(5) {
    DiscountRedemptionCategory(
        id = it.hashCode().toString(),
        categoryName = "Category $it",
        state = when (it) {
            in 0..1 -> DiscountRedemptionChip.STATE_DEFAULT
            in 2..4 -> DiscountRedemptionChip.STATE_INACTIVE
            else -> DiscountRedemptionChip.STATE_DEFAULT
        }
    )
}
```

## Dynamic Item Updates
To update the chips:
```kotlin
chipGroup.items = updatedCategoryList
```
## Programmatic Selection
You can programmatically select and scroll to a specific position:
```kotlin
chipGroup.setSelectedPosition(index)
```
It triggers:

- UI selection state update
- Center scroll via snapping

## Data Model
Ensure you're using the following model:
```kotlin
data class DiscountRedemptionCategory(
    var id: String,
    var categoryName: String,
    @DiscountRedemptionChipStateAnnotation var state: Int,
)
```

## Delegate Callbacks
Implement DiscountRedemptionChipGroupDelegate to handle item selection:
```kotlin
interface DiscountRedemptionChipGroupDelegate {
    fun onItemSelected(position: Int, view: View, item: DiscountRedemptionCategory)
}
```
```kotlin
binding.chipGroup.delegate = object : DiscountRedemptionChipGroupDelegate {
    override fun onItemSelected(
        position: Int,
        view: View,
        item: DiscountRedemptionCategory
    ) {
        // Extension function to manually fake scroll to shows animation
        binding.viewPager.setCurrentItem(position, 350L)
        // binding.viewPager.setCurrentItem(position, true)
    }
}
```

