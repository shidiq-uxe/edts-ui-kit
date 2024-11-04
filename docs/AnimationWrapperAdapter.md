AnimationWrapperAdapter
=======================

The `AnimationWrapperAdapter` is a custom adapter for `RecyclerView` that adds animated transitions to items as they are displayed. It wraps another `RecyclerView.Adapter` and applies animations based on the specified `AnimationType`. This allows for a more dynamic user experience when scrolling through lists.

Features
--------

-   **Multiple Animation Types**: Supports fade, scale, and slide-in animations.
-   **Configurable Animation Duration**: Set the duration for animations globally.
-   **Animation Control**: Options to enable or disable animations during scrolling.
-   **Stable IDs**: Supports stable IDs from the wrapped adapter.

Usage
-----

### 1\. Initialize AnimationWrapperAdapter

You can wrap an existing `RecyclerView.Adapter` with `AnimationWrapperAdapter` as follows:

```kotlin
val myAdapter = MyCustomAdapter()
val animationAdapter = AnimationWrapperAdapter(myAdapter, AnimationType.Scale())

recyclerView.adapter = animationAdapter
```

### 2\. Customize Animations

You can customize the animations by setting the animation type, duration, and interpolator:

```kotlin
animationAdapter.setAnimationType(AnimationType.Fade(startOpacity = 0.5f))
animationAdapter.setDuration(400) // Set duration to 400ms
animationAdapter.setInterpolator(OvershootInterpolator()) // Set custom interpolator`
```

### 4\. Control Animation Behavior

You can choose whether to animate items during scrolling:

```kotlin
animationAdapter.shouldAnimateDuringScroll(true) // Enable animations during scroll`
```

### Animation Types

The following animation types are supported:

| Animation Type               | Description                                       |
|------------------------------|---------------------------------------------------|
| `AnimationType.Fade`         | Fades in the view from a specified opacity.       |
| `AnimationType.Scale`        | Scales the view from a smaller size to full size. |
| `AnimationType.SlideInLeft`  | Slides the view in from the left.                 |
| `AnimationType.SlideInRight` | Slides the view in from the right.                |

### Example Animation Usage

```kotlin
val animationAdapter = AnimationWrapperAdapter(myAdapter, AnimationType.SlideInLeft())
recyclerView.adapter = animationAdapter
```

Notes
-----

-   **Animation Lifecycle**: The animations are triggered when the views are bound and when they enter the screen.
-   **Performance**: Be cautious with animations in long lists, as it may impact scrolling performance. You can disable animations during scrolling if necessary.