import {EmitterSubscription} from 'react-native';

export enum Events {
    EXIT = 'onExit',
    ENTER = 'onEnter',
}

export interface Boundary {
    id: string;
    lat: number;
    lng: number;
    radius: number;
}

export interface BoundaryStatic {
    on: (event: Events, callback: (boundaries: string[]) => void) => EmitterSubscription;
    off: (event: Events) => void;
    add: (boundary: Boundary) => Promise<string>;
    remove: (id: string) => void;
    removeAll: () => void;
}

declare let Boundary: BoundaryStatic;
export default Boundary;
