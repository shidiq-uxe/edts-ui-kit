# DoubleArcProgressIndicator

The `DoubleArcProgressIndicator` is a custom view component for Android, designed to show an animated circular progress indicator with two rotating half-arcs. It supports various customization options like arc thickness, rotation speed, padding, and colors.

## Features

-   **Dual Arc Design**: Displays two rotating half-arcs, creating a dynamic visual effect.
-   **Smooth Animation**: Automatically animates at 60 frames per second with customizable rotation speed.
-   **Customizable Attributes**: Control the arc thickness, size, color, and padding.

## Indicator Overview

![Indicator](https://res.cloudinary.com/dmduc9apd/image/upload/v1730966135/Progress%20Indicator/vfy4tpduxa9zkzdptaec.gif)

## Usage

### 1\. Add to Layout XML

To include the `DoubleArcProgressIndicator` in your layout, add the following XML snippet:

```xml
<com.example.DoubleArcProgressIndicator
    android:id="@+id/doubleArcProgressIndicator"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:innerArcPadding="20dp"
    app:progressThickness="10dp"
    app:progressSize="50dp"
    app:rotateSpeed="6"
    app:arcSweepAngle="270"
    app:progressColor="@color/primary_30"/>
```

### 2\. Programmatic Setup

You can also create and customize the `DoubleArcProgressIndicator` in Kotlin code:

```kotlin
val doubleArcProgressIndicator = DoubleArcProgressIndicator(context).apply {
    progressColor = ContextCompat.getColor(context, R.color.primary_30)
    innerArcPadding = 20f
    progressThickness = 10f
    progressSize = 100f
    rotateSpeed = 8f
    arcSweepAngle = 270f
}
```

### 3\. Control Animation

You can manually start or stop the animation as needed:

```kotlin
// Start animation
doubleArcProgressIndicator.startAnimation()

// Stop animation
doubleArcProgressIndicator.stopAnimation()
```

### Custom Attributes

| Attribute           | Description                                     | Default            |
|---------------------|-------------------------------------------------|--------------------|
| `innerArcPadding`   | Padding between the outer and inner arcs.       | `20f`              |
| `progressThickness` | Thickness of the arc strokes.                   | `10f`              |
| `progressSize`      | Size of the overall indicator (width & height). | `50dp`             |
| `rotateSpeed`       | Speed of arc rotation (degrees per frame).      | `6f`               |
| `arcSweepAngle`     | Sweep angle of the arcs, controls arc length.   | `270f`             |
| `progressColor`     | Color of the arcs (outer and inner).            | `primary_30` color |

### Example Image

### Notes

-   **Animation Lifecycle**: Animation will pause and resume based on view visibility and lifecycle.
-   **60 FPS Animation**: Runs at ~60 frames per second, updating every 16ms.