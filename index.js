import {NativeEventEmitter, NativeModules, AppRegistry} from 'react-native';

const {RNBoundary} = NativeModules;

const TAG = "RNBoundary";

const boundaryEventEmitter = new NativeEventEmitter(RNBoundary);

const Events = {
  EXIT: "onExit",
  ENTER: "onEnter",
};

export {
  Events
}

const HeadlessBoundaryEventTask = async ({event, id}) => {
  console.log(event, id);
  boundaryEventEmitter.emit(event, id)
};

AppRegistry.registerHeadlessTask('OnBoundaryEvent', () => HeadlessBoundaryEventTask);

export default {
  add: boundary => {
    if (!boundary || (boundary.constructor !== Array && typeof boundary !== 'object')) {
      throw TAG + ': a boundary must be an array or non-null object';
    }
    return new Promise((resolve, reject) => {
      if (typeof boundary === 'object' && !boundary.id) {
        reject(TAG + ': an id is required')
      }

      RNBoundary.add(boundary)
        .then(id => resolve(id))
        .catch(e => reject(e))
    })
  },

  on: (event, callback) => {
    if (typeof callback !== 'function') {
      throw TAG + ': callback function must be provided';
    }
    if (!Object.values(Events).find(e => e === event)) {
      throw TAG + ': invalid event';
    }

    return boundaryEventEmitter.addListener(event, callback);
  },

  off: (event) => {
    if (!Object.values(Events).find(e => e === event)) {
      throw TAG + ': invalid event';
    }

    return boundaryEventEmitter.removeAllListeners(event);
  },

  removeAll: () => {
    return RNBoundary.removeAll();
  },

  remove: id => {
    if (!id || (id.constructor !== Array && typeof id !== 'string')) {
      throw TAG + ': id must be a string';
    }

    return RNBoundary.remove(id);
  }
}

