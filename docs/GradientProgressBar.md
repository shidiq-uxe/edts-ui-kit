Gradient Progress Bars
======================

Gradient Progress Bar provides two customizable progress bar views:

-   **GradientLinearProgressBar**: A linear progress bar with gradient support.
-   **GradientCircularProgressBar**: A circular progress bar with gradient support.

Both classes allow control over properties such as colors, thickness, padding, and animation. Additionally, an `onAnimationUpdateListener` interface is available for custom animation updates.

Features
--------

-   **Gradient Progress Indicator**: Customize with a gradient effect from start to end color.
-   **Track Customization**: Set thickness, padding, and color of the track.
-   **Smooth Animation**: Animate progress updates, with optional listener.

Usage
-----

### 1\. Add to Layout XML

To use either progress bar, include it in your XML layout file.

#### GradientLinearProgressBar

```xml
<com.example.GradientLinearProgressBar
    android:id="@+id/gradientLinearProgressBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:indicatorStartColor="@color/primary_20"
    app:indicatorEndColor="@color/primary_40"
    app:trackColor="@color/black_20"
    app:trackThickness="20dp"
    app:trackPadding="4dp"
    app:trackCornerRadius="10dp"
    app:indicatorProgress="50"
    app:maxIndicatorProgress="100"
    app:shouldAnimate="true"/>
```

#### GradientCircularProgressBar

```xml
<com.example.GradientCircularProgressBar
    android:id="@+id/gradientCircularProgressBar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:indicatorStartColor="@color/primary_20"
    app:indicatorEndColor="@color/primary_40"
    app:trackColor="@color/black_20"
    app:trackThickness="20dp"
    app:trackPadding="4dp"
    app:indicatorProgress="50"
    app:maxIndicatorProgress="100"
    app:shouldAnimate="true"/>
```

### 2\. Programmatic Setup

You can also set up either progress bar directly in Kotlin code:
```kotlin
val linearProgressBar = GradientLinearProgressBar(context).apply {
    indicatorProgress = 75f
    maxIndicatorProgress = 100f
    indicatorStartColor = ContextCompat.getColor(context, R.color.primary_20)
    indicatorEndColor = ContextCompat.getColor(context, R.color.primary_40)
    trackColor = ContextCompat.getColor(context, R.color.black_20)
    trackThickness = 20f
    trackPadding = 4f
    shouldAnimate = true
}
```

### 3\. Set Progress Programmatically

To update progress in real-time for either progress bar:

```kotlin
gradientLinearProgressBar.indicatorProgress = 60f
gradientCircularProgressBar.indicatorProgress = 85f
```

### 4\. Delegate Listener

To listen for progress animation updates, implement the `GradientProgressBarDelegate` interface:

```kotlin
class MyProgressBarListener : GradientProgressBarDelegate {
  override fun onAnimationUpdateListener(view: View, currentProgressValue: Float, finalProgressValue: Float) {
  // Handle animation updates here
  }
}
```

// Usage
```kotlin
gradientLinearProgressBar.delegate = MyProgressBarListener()
gradientCircularProgressBar.delegate = MyProgressBarListener()
```

### 5\. Image Placeholders

1.  **Linear Progress Example:**

2.  **Circular Progress Example:**

Custom Attributes
-----------------

| Attribute              | Description                               | Default            |
|------------------------|-------------------------------------------|--------------------|
| `indicatorProgress`    | Sets the current progress.                | `0f`               |
| `maxIndicatorProgress` | Sets the maximum progress.                | `100f`             |
| `indicatorStartColor`  | Start color of the gradient indicator.    | `primary_20` color |
| `indicatorEndColor`    | End color of the gradient indicator.      | `primary_40` color |
| `trackColor`           | Color of the track (background).          | `black_20` color   |
| `trackThickness`       | Thickness of the track.                   | `20f`              |
| `trackCornerRadius`    | Corner radius of the track (linear only). | `10f`              |
| `trackPadding`         | Padding around the track.                 | `0f`               |
| `shouldAnimate`        | Enables or disables progress animation.   | `true`             |

Example Scenarios
-----------------

-   **Loading Indicator**: Use as a loading indicator in any progress-based UI.
-   **Step Progress Indicator**: Track the progress of specific tasks.