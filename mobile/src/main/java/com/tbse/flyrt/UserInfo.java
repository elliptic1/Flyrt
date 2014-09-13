package com.tbse.flyrt;

import android.location.Location;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by tbsmith on 7/21/14.
 */
public class UserInfo {

    public static float lastLocationUpdateTime = 0;

    public static void uploadSelfie(byte[] byteArray) {
        ParseUser.getCurrentUser().put("image", byteArray);

        SaveCallback saveImageCallback = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("flyrt", "done with image save.");
                if (e != null) {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        Log.d("flyrt", "error: object not found");
                    } else {
                        Log.d("flyrt", "error is "+ e.getCode() + ": " + e.getMessage());
                    }
                }
            }
        };

        ParseUser.getCurrentUser().saveInBackground(saveImageCallback);

    }

    public static void saveLocationToCloud(Location location) {
        ParseGeoPoint pgp = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        ParseUser.getCurrentUser().put("location", pgp);
        ParseUser.getCurrentUser().saveInBackground(saveLocationCallback);
    }

    static SaveCallback saveLocationCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            Log.d("flyrt", "done with location save.");
            if (e != null) {
                HomeActivity.resetUI();
                Log.e("flyrt", "error is " + e.getCode() + ": " + e.getMessage());
            } else {
                lastLocationUpdateTime = System.currentTimeMillis();
            }

        }
    };
}
