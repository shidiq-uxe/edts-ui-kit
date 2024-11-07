# Interpolator

## Introduction

An **Interpolator** is a crucial component in animation, enabling developers to control the rate of change during an animation sequence. By defining the progression of an animation, interpolators shape the visual experience, making animations feel more natural or adding creative effects.

## What is an Interpolator?

In Android development, an interpolator determines how an animation's values change over time. For example, it defines whether the animation should speed up, slow down, bounce, or follow a custom path. This fine-tunes the look and feel of animations, making them more engaging and fluid.

### How Interpolators Work

An interpolator manipulates the fraction of the duration that has elapsed, altering the speed of animation. By customizing the interpolator's path, you can achieve effects such as ease-in, ease-out, or more complex easing curves.

### Visual Demonstration with Image Placeholders

Below are demonstrations showcasing how different interpolators affect image transitions, creating a smoother or more dynamic animation.

| Interpolator Type       | Demo |
|-------------------------|------|
| LinearInterpolator      |      |
| EaseInQuadInterpolator  |      |
| EaseOutBackInterpolator |      |

## Default Interpolators in Android

Android comes with built-in interpolators that developers commonly use, including:

-   **LinearInterpolator**: Animates at a constant rate.
-   **AccelerateInterpolator**: Starts slow and accelerates towards the end.
-   **DecelerateInterpolator**: Starts fast and decelerates towards the end.
-   **AccelerateDecelerateInterpolator**: Starts slow, speeds up in the middle, and slows down at the end.
-   **BounceInterpolator**: Simulates a bouncing effect.

## Custom Interpolators with `EaseInterpolator`

For more tailored animations, the `EaseInterpolator` object provides a collection of predefined custom easing functions:

### `EaseInterpolator` Class

The `EaseInterpolator` class includes a set of interpolators created using `PathInterpolator` to produce various easing effects:

```kotlin
object EaseInterpolator {
    // Ease In Interpolators
    val EaseInQuadInterpolator = PathInterpolator(0.550f, 0.085f, 0.680f, 0.530f)
    val EaseInQubicInterpolator = PathInterpolator(0.550f, 0.055f, 0.675f, 0.190f)
    // ...more interpolators...

    // Ease Out Interpolators
    val EaseOutQuadInterpolator = PathInterpolator(0.250f, 0.460f, 0.450f, 0.940f)
    val EaseOutQubicInterpolator = PathInterpolator(0.215f, 0.610f, 0.355f, 1.000f)
    // ...more interpolators...

    // Ease In Out Interpolators
    val EaseInOutQuadInterpolator = PathInterpolator(0.455f, 0.030f, 0.515f, 0.955f)
    val EaseInOutQubicInterpolator = PathInterpolator(0.645f, 0.045f, 0.355f, 1.000f)
    // ...more interpolators...
}
```

### How to Use `Interpolator`

To use an interpolator, simply assign it to your animation as shown below:

```kotlin

val animation = ObjectAnimator.ofFloat(view, "translationX", 0f, 100f).apply {
    duration = 1000L
    interpolator = LinearInterpolator() // or EaseInterpolator.EaseInOutQubicInterpolator 
}
animation.start()
```