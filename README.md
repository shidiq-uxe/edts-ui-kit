# UI Kits and Components Library

This library provides a set of UI Kits and Components designed for production use in Android applications. The library is currently under development, with ongoing efforts to expand its features and improve stability. Below is an overview of the components and utilities that have been developed so far.

## Installation

To include this library in your project, add the following dependency to your `build.gradle` file:

```groovy
dependencies {
    implementation 'com.edts:uikits:$latestVersion'
}
```

## Table of Contents
1. [Typography & TextView](docs/Typography.md)
2. [Button Styles](docs/Button.md)
3. [Dialog](docs/Dialog.md)
4. [Snackbar Alerts](docs/Alert.md)
5. [Base Adapter](docs/BaseAdapter.md)
6. [Textfield](docs/TextField.md)
7. [Indicator View](docs/IndicatorView.md)
8. [Boarding View](docs/BoardingPagerView.md)
9. [ProgressView for TextView and Button](docs/ProgressView.md)
10. [BottomTray/BottomSheet](docs/BottomTray.md)
11. [Utils Extensions](docs/UtilsExtensions.md)

## Basic setup and initialization :

You could replace PopUpTheme under module manifest file with themes down bellow to ensure all component used the same Theme :

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