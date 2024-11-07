# SearchBar

### Overview

`SearchBar` is a customizable and interactive UI component designed for search functionalities in Android applications. It features a variety of attributes to modify appearance, behavior, and animations.

### Features

-   Customizable placeholder with animated transitions.
-   Support for multiple input types (Text, Phone, Email).
-   Prefix text support with typewriter and slide-up animations.
-   Interactive start and end icons with visibility options.
-   Custom animations for end icon when text changes.
-   Support for dynamic hint text and placeholder text arrays.
-   Delegate pattern for handling search bar interactions.

### SearchBar Animation Overview
| SlideUp                                                                                                    | TypeWriter                                                                                                 | TypeWriterWithPrefix                                                                                        |
|------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| ![SU](https://res.cloudinary.com/dmduc9apd/image/upload/v1730969627/Search%20Bar/dbnclzipdb8k68i6nndz.gif) | ![TW](https://res.cloudinary.com/dmduc9apd/image/upload/v1730969627/Search%20Bar/tntuws5hvmrnodiwtaom.gif) | ![TWP](https://res.cloudinary.com/dmduc9apd/image/upload/v1730969627/Search%20Bar/mgtme6yqx6huv7401oqz.gif) |


### Attributes and Properties

#### XML Attributes

-   `closeIconVisible` (boolean): Controls visibility of the end icon (close icon).
-   `fieldMaxLength` (integer): Sets the maximum number of characters allowed in the input field.
-   `fieldInputType` (enum): Specifies the input type (Text, Phone, Email).
-   `prefixText` (string): Sets the prefix text to display.
-   `placeholderTextAppearance` (reference): Customizes the appearance of placeholder text.
-   `startIcon` (reference): Specifies the drawable resource for the start icon.
-   `placeholderTextList` (reference): A string array resource ID for rotating placeholder texts.

#### Public Properties

-   `text`: Sets or retrieves the current input text.
-   `fieldMaxLength`: Limits the length of input text.
-   `fieldInputType`: Specifies the input type of the field.
-   `isStartIconVisible`: Controls visibility of the start icon.
-   `startIcon`: Sets the drawable resource for the start icon.
-   `isCancelVisible`: Toggles the visibility of the end icon.
-   `prefixText`: Sets the prefix text to display.
-   `placeholderTexts`: An array of placeholder texts that animate in rotation.
-   `placeholderTextAppearance`: Changes the text appearance of the placeholder.
-   `placeholderTextColor`: Customizes the placeholder text color.
-   `shouldAnimatePlaceholder`: Toggles placeholder animation.
-   `placeholderAnimationType`: Enum to choose between `SlideUp`, `TypeWriter`, or `TypeWriterWithPrefix` animations.

### Delegate Interface

```kotlin
interface SearchBarDelegate {
    fun onTextChange(newText: String)
    fun onCloseIconClicked(view: View)
    fun onFocusChange(view: View, hasFocus: Boolean)
}
```

### Placeholder Animations

-   **SlideUp**: Slides up the placeholder text at regular intervals.
-   **TypeWriter**: Simulates typewriter animation to reveal each character.
-   **TypeWriterWithPrefix**: Similar to `TypeWriter` but includes a scaling effect.

#### Event Listeners

-   `onEditTextFocusChangeListener()`: Listens to changes in focus on the input field.
-   `onEditTextTextChangeListener()`: Listens for changes in the input text and handles animations.

### Usage Example

#### XML Layout

```xml
<com.example.SearchBar
    android:id="@+id/search_bar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:closeIconVisible="true"
    app:fieldMaxLength="50"
    app:fieldInputType="Text"
    app:prefixText="Search"
    app:placeholderTextList="@array/placeholder_texts" />
```

#### Kotlin Code

```kotlin
val searchBar = findViewById<SearchBar>(R.id.search_bar)
searchBar.searchBarDelegate = object : SearchBarDelegate {
    override fun onTextChange(newText: String) {
        // Handle text changes
    }

    override fun onCloseIconClicked(view: View) {
        // Handle close icon click
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        // Handle focus change
    }
}
```

### Customization Tips

-   Use `placeholderAnimationType` to change the animation style to match your design needs.
-   Customize the appearance with `placeholderTextAppearance` for consistent branding.
-   Adjust `fieldMaxLength` and `fieldInputType` for specific input validations.

### Notes

-   Ensure `placeholderTexts` is provided when `shouldAnimatePlaceholder` is true.
-   For `TypeWriterWithPrefix`, `prefixText` must be set to avoid exceptions.

### Troubleshooting

-   **Animation Issues**: Verify `placeholderAnimationType` and `placeholderTexts` are set correctly.
-   **Icon Visibility**: Ensure `startIcon` and `isStartIconVisible` or `isCancelVisible` are configured as needed.