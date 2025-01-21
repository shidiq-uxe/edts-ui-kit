HomeTabLayout
=============

`HomeTabLayout` is a custom `FrameLayout` for Android that provides a three-tab layout with animated transitions and visual effects. It allows developers to create visually appealing tabs with smooth animations and delegate callbacks for tab selection changes.

* * * * *

Features
--------

-   **Three tabs**: Grocery, Food, and Virtual.

-   **Smooth animations**: Includes animations for tab selection, active button repositioning, and shape appearance changes.

-   **Delegate pattern**: Provides a callback mechanism to notify when a tab is selected.

* * * * *

Preview
-------
![HPTL](https://res.cloudinary.com/dmduc9apd/image/upload/v1736496805/Home%20Tab%20Layout/wpiun8asayvscatyt3aw.gif)

Installation
------------

### 1\. Add HomeTabLayout to your layout XML:

```xml
<id.co.edtslib.uikit.tablayout.HomeTabLayout
    android:id="@+id/homeTabLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

### 2\. Reference it in your Activity/Fragment:

```kotlin
val homeTabLayout = findViewById<HomeTabLayout>(R.id.homeTabLayout)
```

* * * * *

Usage
-----

### 1\. Setting the Delegate

To listen to tab selection changes, set a `HomeTabLayoutDelegate`:

```kotlin
homeTabLayout.delegate = object : HomeTabLayoutDelegate {
    override fun onTabSelected(tab: HomeTabLayout.HomeTab) {
        // Handle tab selection
        when (tab) {
            HomeTabLayout.HomeTab.Grocery -> // Todo...
                HomeTabLayout.HomeTab.Food -> // Todo...
            HomeTabLayout.HomeTab.Virtual -> // Todo...
        }
    }
}
```

### 2\. Accessing the Current Tab

The current tab is stored in the `homeTab` property:

```kotlin
val currentTab = homeTabLayout.homeTab
```

* * * * *

Delegate Interface
------------------

To implement the delegate pattern, use the following interface:

```kotlin
interface HomeTabLayoutDelegate {
    fun onTabSelected(tab: HomeTabLayout.HomeTab)
}
```

* * * * *

Possible Customization
-------------

### 1\. Changing Tab Text or Style

Modify the `MaterialButton` properties in your layout or programmatically:

```kotlin
homeTabLayout.findViewById<MaterialButton>(R.id.tab1).apply {
    text = "Custom Tab Name"
    setTextColor(Color.RED)
}
```

* * * * *

Dependencies
------------

-   **Material Components**: Ensure you have the Material Design library in your `build.gradle` file:

```groovy
implementation 'com.google.android.material:material:1.9.0'
```

* * * * *
