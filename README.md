
# react-native-boundary

### Note: Currently in development. Will be releasing an MVP soon.

## Getting started

`$ npm install react-native-boundary --save`

### Mostly automatic installation

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
  	project(':react-native-boundary').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-boundary/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-boundary')
  	```

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
  }
  
  componentWillUnmount() {
    Boundary.removeAll()
      .then(() => console.log("success!"))
      .catch(e => console.error("error :(", e));
  }
}
```
  