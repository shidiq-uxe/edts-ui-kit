# HomeSwitcher Component

`HomeSwitcher` is a customizable Android UI component that provides a two-tab switcher with animations and delegate callbacks. It is designed to enhance UX by offering a smooth and visually appealing tab-switching experience.

---

## Features

- **Customizable Titles and Subtitles**: Update tab titles and subtitles dynamically.

- **Customizable Icons**: Set unique icons for each tab.

- **Smooth Animations**: Includes spring animations and color transitions for a polished user experience.

- **Delegate Support**: Notify listeners about tab-switch events.

- **XML Attribute Support**: Configure properties directly in XML layouts.

## Preview
![Switcher](https://res.cloudinary.com/dmduc9apd/image/upload/v1737527968/Switcher_fvdgg8.gif)

## Installation

Include the `HomeSwitcher` component in your layout file:

```xml
<id.co.edtslib.uikit.switcher.HomeSwitcher 
    android:id="@+id/homeSwitcher"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:firstTabTitle="Xpress"
    app:firstTabSubtitle="Fast Delivery"
    app:firstTabIcon="@drawable/ic_flash_xpress_24"
    app:secondTabTitle="Xtra"   
    app:secondTabSubtitle="Extra Features" 
    app:secondTabIcon="@drawable/ic_box_xtra_16" />
```

* * * * *

## Usage

### 1\. Setup in Kotlin

Add the `HomeSwitcher` to your layout and configure it programmatically in your Activity or Fragment:

```kotlin
val homeSwitcher = findViewById<HomeSwitcher>(R.id.homeSwitcher)

homeSwitcher.firstTabTitle = "Custom Xpress Title"
homeSwitcher.firstTabSubtitle = "Custom Xpress Subtitle"
homeSwitcher.firstTabIcon = R.drawable.custom_xpress_icon
homeSwitcher.secondTabTitle = "Custom Xtra Title"
homeSwitcher.secondTabSubtitle = "Custom Xtra Subtitle"
homeSwitcher.secondTabIcon = R.drawable.custom_xtra_icon

homeSwitcher.delegate = object : HomeSwitcherDelegate {
    override fun setOnSwitchChangedListener(tab: HomeSwitcher.Tab) {
        when (tab) {
            HomeSwitcher.Tab.Xpress -> {
                // Handle Xpress tab selected
            }

            HomeSwitcher.Tab.Xtra -> {
                // Handle Xtra tab selected
            }
        }
    }
}
```

* * * * *

### 2\. XML Attributes

You can configure titles, subtitles, and icons directly in XML using the following attributes:

| Attribute Name      | Description                 | Default Value                  |
|---------------------|-----------------------------|--------------------------------|
| `firstTabTitle`     | Title for the first tab     | `"Xpress"`                     |
| `firstTabSubtitle`  | Subtitle for the first tab  | `"Fast Delivery"`              |
| `firstTabIcon`      | Icon for the first tab      | `@drawable/ic_flash_xpress_24` |
| `secondTabTitle`    | Title for the second tab    | `"Xtra"`                       |
| `secondTabSubtitle` | Subtitle for the second tab | `"Extra Features"`             |
| `secondTabIcon`     | Icon for the second tab     | `@drawable/ic_box_xtra_16`     |

* * * * *

### 3\. Delegate

Implement the `HomeSwitcherDelegate` to listen for tab change events:

```kotlin
interface HomeSwitcherDelegate {
    fun setOnSwitchChangedListener(selectedTab: HomeSwitcher.Tab)
    fun setOnSwitchAnimationEndListener(selectedTab: HomeSwitcher.Tab)
}
```

* * * * *

## Example

```kotlin
homeSwitcher.delegate = object : HomeSwitcherDelegate { 
    override fun setOnSwitchChangedListener(tab: HomeSwitcher.Tab) { 
        when (tab) {
            HomeSwitcher.Tab.Xpress -> {
                // Perform action for Xpress tab
            }

            HomeSwitcher.Tab.Xtra -> {
                // Perform action for Xtra tab
            } 
        }
    }
    
    override fun setOnSwitchAnimationEndListener(tab: HomeSwitcher.Tab) {
        // Customize when animation end, such as displaying Skeleton(Shimmer)
    }
}
```

* * * * *

## Customization

### Programmatic Updates

You can dynamically update the titles, subtitles, and icons at runtime:

```kotlin
homeSwitcher.firstTabTitle = "Updated Xpress Title"
homeSwitcher.firstTabSubtitle = "Updated Subtitle"
homeSwitcher.firstTabIcon = R.drawable.new_xpress_icon
```

## Animation

The component includes a spring animation for smooth transitions. As for now, you cannot modify the stiffness and damping ratio in the `SpringForce` instance.

* * * * *

## Requirements

*Dependencies**: Material Components 1.9.0 for Android.