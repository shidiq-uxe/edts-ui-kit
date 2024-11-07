# UI Kits and Components Library

This library provides a set of UI Kits and Components designed for production use in Android applications. The library is currently under development, with ongoing efforts to expand its features and improve stability. Below is an overview of the components and utilities that have been developed so far.

## Installation

To include this library in your project, add the following dependency to your `build.gradle` file:

[![](https://jitpack.io/v/shidiq-uxe/edts-ui-kit.svg)](https://jitpack.io/#shidiq-uxe/edts-ui-kit)

```groovy
dependencies {
    implementation 'com.edts:uikits:$latestVersion'
}
```

## Table of Contents
1. [Typography & TextView](docs/Typography.md)
2. [Button](docs/Button.md)
3. [Dialog](docs/Dialog.md)
4. [PopUp](docs/Popup.md)
5. [AlertBox](docs/AlertBox)
6. [Snackbar Alerts](docs/Snackbar.md)
7. [Base Adapter](docs/BaseAdapter.md)
8. [Animation Wrapper Adapter](docs/AnimationWrapperAdapter.md)
9. [Textfield](docs/TextField.md)
10. [OTP Group](docs/OtpGroup.md)
11. [Search Bar](docs/Searchbar.md)
12. [Indicator View](docs/IndicatorView.md)
13. [Boarding View](docs/BoardingPagerView.md)
14. [ProgressView for TextView and Button](docs/ProgressView.md)
15. [Double Arc Progress Indicator](docs/DoubleArcProgressIndicator.md)
16. [Gradient ProgressBar](docs/GradientProgressBar.md)
17. [Liquid Refresh Layout](docs/LiquidRefreshLayout.md)
18. [BottomTray/BottomSheet](docs/BottomTray.md)
19. [Interpolator](docs/Interpolator.md)
20. [Utils Extensions](docs/UtilsExtensions.md)
21. [Spannable Extensions](docs/UtilsExtensions.md)
22. [String Builder Extensions](docs/UtilsExtensions.md)

## Basic setup and initialization :

You could replace PopUpTheme under module manifest file for Activity Tag with themes down bellow to ensure all component used the same Theme :

```xml
  <activity
        android:name="edts.klikidm.android.feature.name.Activity"
        android:configChanges="orientation|keyboardHidden|screenSize"
        android:exported="true"
        android:label="@string/input_account"
        android:screenOrientation="portrait"
        android:theme="@style/Theme.EDTS.UIKit.ActionBar" 
        android:windowSoftInputMode="adjustResize"
        tools:ignore="LockedOrientationActivity"/>
```


| **Theme**                                         | **Material3**                     | **Description**          |
|---------------------------------------------------|-----------------------------------|--------------------------|
| [Theme.EDTS.UIKit](docs/ActionBar.md)             | Theme.Material3.Light             | With Action Bar Included |
| [Theme.EDTS.UIKit.NoActionBar](docs/ActionBar.md) | Theme.Material3.Light.NoActionBar | Without Action Bar       |


    
## Roadmap

- **Complete the Shape / Radius utilities.** -
- **Adjust Snackbar Design** -
- **Continue development on the Coach Mark View.** -
- **Further refine the Bottom Tray / Bottom Sheet component.** -
- **Further refine the TextField component.** -

## Contributing
Contributions are welcome! If you'd like to contribute, please fork the repository and submit a pull request.