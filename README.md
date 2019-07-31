
# react-native-boundary

A simple, native, and efficient geofencing/region monitoring react native library for both iOS and android. 

## Usage
```javascript
import Boundary, {Events} from 'react-native-boundary';

class MyComponent extends Class {
  componentWillMount() {
    Boundary.add({
      lat: 34.017714,
      lng: -118.499033,
      radius: 50, // in meters
      id: "Chipotle",
    })
      .then(() => console.log("success!"))
      .catch(e => console.error("error :(", e));
   
    Boundary.on(Events.ENTER, id => {
      // Prints 'Get out of my Chipotle!!'
      console.log(`Get out of my ${id}!!`);
    });
    
    Boundary.on(Events.EXIT, id => {
      // Prints 'Ya! You better get out of my Chipotle!!'
      console.log(`Ya! You better get out of my ${id}!!`)
    })
  }
  
  componentWillUnmount() {
    // Remove the events
    Boundary.off(Events.ENTER)
    Boundary.off(Events.EXIT)

    // Remove the boundary from native API´s
    Boundary.remove('Chipotle')
      .then(() => console.log('Goodbye Chipotle :('))
      .catch(e => console.log('Failed to delete Chipotle :)', e))
  }
}
```
## Getting started

`$ npm install react-native-boundary --save`

### Automatic Installation

`$ react-native link react-native-boundary`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-boundary` and add `RNBoundary.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBoundary.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.eddieowens.RNBoundaryPackage;` to the imports at the top of the file
  - Add `new RNBoundaryPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-boundary'
  	project(':react-native-boundary').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-boundary/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-boundary')
  	```
  	
### Post Install

#### Android

Add the following permission, services and receivers to your `AndroidManifest.xml` like so:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="com.mypackage">
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    ...
    <application>
        <!-- Services -->
        <service
            android:name="com.eddieowens.services.BoundaryEventJobIntentService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_JOB_SERVICE" />
        <service android:name="com.eddieowens.services.BoundaryEventHeadlessTaskService" />

        <!-- Receivers -->
        <receiver
            android:name="com.eddieowens.receivers.BoundaryEventBroadcastReceiver"
            android:enabled="true" />

        ...
    </application>
</manifest>
```

#### iOS
Before iOS 11:
Add the following to your `Info.plist`:
- `NSLocationAlwaysUsageDescription`

For iOS 11:
Add the following to your `Info.plist`:
- `NSLocationWhenInUseUsageDescription`
- `NSLocationAlwaysAndWhenInUseUsageDescription`

## API

### Functions
Name        | Arguments                                     | Note
----------- | --------------------------------------------- | ---
`on`        | event: [event](#events), callback: `function` | Triggers the callback when the `event` occurs. The callback will be passed an array of boundary ids as `strings`. Can be called in the background
`off`       | event: [event](#events)                       | Removes bound event listeners
`add`       | boundary: [boundary](#boundary)               | Adds a `Boundary` that can be triggered when an [event](#events) occurs
`remove`    | id: `string`                                  | Removes a Boundary from being triggered. Boundaries will remain until `remove` or `removeAll` is called or the app is uninstalled
`removeAll` | `void`                                        | Removes all boundaries and event callbacks.

### Types
#### Boundary
Field    | Type     | Note
-------- | -------- | ----
`id`     | `string` | ID for your boundary. Value that is returned when an [event](#events) is triggered
`lat`    | `number` | Must be a valid latitude
`lng`    | `number` | Must be a valid longitude
`radius` | `number` | In meters. It is highly suggested that the `radius` is greater than 50 meters

#### Events
Field    | Type      | Note
-------- | --------- | ----
`ENTER`  | `string`  | Event for when a user enters a [boundary](#boundary)  
`EXIT`   | `string`  | Event for when a user exits a [boundary](#boundary)

