package com.eddieowens.errors;

import com.google.android.gms.location.GeofenceStatusCodes;

public class GeofenceErrorMessages {
    private GeofenceErrorMessages() {}


    /**
     * Returns the error string for a geofencing error code.
     */
    public static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence is not available. Android emulator requires Settings -> Location -> Mode to be set at 'Battery Saving' or 'High Accuracy'";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error: " + Integer.toString(errorCode);
        }
    }
}
