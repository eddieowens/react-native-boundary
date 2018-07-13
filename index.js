import {DeviceEventEmitter, NativeModules} from 'react-native';

const {RNBoundary} = NativeModules;

const TAG = "RNBoundary";

const Events = {
  EXIT: "onExit",
  ENTER: "onEnter",
};

export {
  Events
}

export default {
  add: geofence => {
    if (!geofence || (geofence.constructor !== Array && typeof geofence !== 'object')) {
      throw TAG + ': a geofence must be an array or non-null object';
    }
    RNBoundary.add(geofence);
  },

  on: (event, callback) => {
    if (typeof callback !== 'function') {
      throw TAG + ': callback function must be provided';
    }
    if (!Events.hasOwnProperty(event)) {
      throw TAG + ': invalid event';
    }

    return DeviceEventEmitter.addListener(event, callback);
  },

  removeAll: () => {
    return RNBoundary.removeAll();
  },

  remove: toRemove => {
    if (!toRemove || (toRemove.constructor !== Array && typeof toRemove !== 'object')) {
      throw TAG + ': a geofence must be an array or non-null object';
    }
    return RNBoundary.remove(toRemove);
  }
}

