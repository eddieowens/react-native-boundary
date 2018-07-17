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
  add: boundary => {
    if (!boundary || (boundary.constructor !== Array && typeof boundary !== 'object')) {
      throw TAG + ': a boundary must be an array or non-null object';
    }
    return RNBoundary.add(boundary);
  },

  on: (event, callback) => {
    if (typeof callback !== 'function') {
      throw TAG + ': callback function must be provided';
    }
    if (!Object.values(Events).find(e => e === event)) {
      throw TAG + ': invalid event';
    }

    return DeviceEventEmitter.addListener(event, callback);
  },

  removeAll: () => {
    Object.values(Events).forEach(e => DeviceEventEmitter.removeAllListeners(e));
    return RNBoundary.removeAll();
  },

  remove: id => {
    if (!id || (id.constructor !== Array && typeof id !== 'string')) {
      throw TAG + ': a boundary must be an array or non-null object';
    }

    return RNBoundary.remove(id);
  }
}

