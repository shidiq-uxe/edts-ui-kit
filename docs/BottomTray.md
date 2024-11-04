BottomSheetTray
===============

`BottomSheetTray` is a custom implementation of `BottomSheetDialogFragment`, designed to provide a configurable and interactive bottom sheet experience in Android. It offers a variety of customization options, such as title views, navigation icons, content layouts, and animation configurations.

Class Overview
--------------

### Primary Features:

-   Customizable title and content views
-   Delegate callbacks for behavior monitoring
-   Snap point support for flexible sliding control
-   Adjustable dismiss behavior and animations
-   Options for visibility and styling of navigation and action icons

Properties
----------

### General Properties:

-   **`binding`**: Provides access to `ViewBottomTrayBinding` for binding views.
-   **`delegate`**: A `BottomTrayDelegate` instance to handle callback events.
-   **`title`**: String for the title text shown at the top of the tray.
-   **`customTitleView`**: Optional `View` to override the default title layout.
-   **`contentView`**: `View` representing the main content of the bottom sheet.
-   **`dismissOnOutsideTouch`**: Boolean to allow the sheet to dismiss when touching outside.
-   **`isCancelableOnTouchOutside`**: Boolean indicating whether the sheet is cancelable with outside touch.
-   **`customBackgroundColor`**: Optional color resource for the tray background.
-   **`snapPoints`**: Array of integers defining snap positions for flexible scrolling behavior.
-   **`customAnimationsEnabled`**: Boolean to toggle custom animations on or off.
-   **`dragHandleVisibility`**: Boolean to control visibility of the drag handle.
-   **`titleDividerVisibility`**: Boolean to toggle the title divider's visibility.
-   **`shouldShowNavigation`**: Boolean to control the display of the navigation button.
-   **`shouldShowClose`**: Boolean for showing or hiding the close button.

### Appearance Properties:

-   **`titleTextAppearance`**: Resource ID for the title's text appearance.
-   **`titleTextColor`**: Resource ID for the color of the title text.
-   **`endIcon`**: Drawable resource ID for the end icon (e.g., close button).
-   **`endIconTint`**: Tint color resource ID for the end icon.
-   **`endIconText`**: String resource ID for text shown beside the end icon.
-   **`navigationIcon`**: Drawable resource ID for the navigation icon.
-   **`navigationIconTint`**: Tint color resource ID for the navigation icon.

Methods
-------

### Public Methods:

-   **`getBottomSheetBehavior()`**: Returns the `BottomSheetBehavior<View>` associated with the tray.
-   **`newInstance(title: String?, customTitleLayout: View?, contentLayout: View?)`**:
    -   Factory method to create an instance of `BottomSheetTray` with optional title, custom title layout, and content layout.

### Overridden Methods:

-   **`getTheme()`**: Specifies the theme for the dialog (`R.style.ThemeOverlay_EDTS_UIKit_BottomSheetDialog`).
-   **`onCreateDialog(savedInstanceState: Bundle?)`**: Initializes the dialog and sets up the `BottomSheetBehavior`.
-   **`onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)`**: Inflates the layout for the bottom sheet.
-   **`onViewCreated(view: View, savedInstanceState: Bundle?)`**: Configures the view properties after it's created.
-   **`onDismiss(dialog: DialogInterface)`**: Handles dismiss logic and invokes the delegate's `onDismiss`.

### Private Methods:

-   **`setupBottomSheetBehavior()`**: Configures `BottomSheetBehavior` properties and snapping behavior.
-   **`setCustomAnimations()`**: Placeholder method for enabling custom animations.
-   **`bindTitleView()`**: Applies styles and properties to the title view, including text appearance and icon configuration.

### Callbacks:

-   **`bottomSheetTrayBehavior`**: A `BottomSheetBehavior.BottomSheetCallback` implementation that handles state changes and sliding behavior, supporting snap points.

Delegate Interface
------------------

The `BottomTrayDelegate` interface (not included in the class but referenced) is expected to handle callbacks like `onShow()`, `onStateChanged()`, `onSlide()`, and `onDismiss()` for custom behavior logic.

Companion Object
----------------

-   **`newInstance()`**: Simplifies the creation of a `BottomSheetTray` instance with optional arguments for title and custom views.

Usage Example
-------------

```kotlin
val bottomSheet = BottomSheetTray.newInstance(
    title = "Example Title",
    customTitleLayout = customTitleView,
    contentLayout = contentView
)
bottomSheet.show(supportFragmentManager, "BottomSheetTrayTag")
```

Remarks
-------

-   The `BottomSheetTray` class supports customization to fit various use cases, such as creating complex modal dialogues or simple information sheets.
-   `setCustomAnimations()` is a placeholder, and developers should define custom animations in `styles.xml` for full functionality.