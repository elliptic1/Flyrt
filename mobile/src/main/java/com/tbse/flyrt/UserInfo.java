package com.tbse.flyrt;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

/**
 * Created by tbsmith on 7/21/14.
 */
public class UserInfo {

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor sharedPreferencesEditor;

    public static boolean hasTakenSelfie;
    public static boolean wantsNotifications;

    public static ParseObject userInfo;

    private static Location lastKnownLocation;
    public static float lastLocationUpdateTime = 0;

    private static Context context;

    public UserInfo(Context c) {
        context = c;
    }

    public void init() {

        sharedPreferences = context.getSharedPreferences("Flyrt", Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        hasTakenSelfie = false;

    }

    public void uploadSelfie(byte[] byteArray) {
        getUserInfo().put("image", byteArray);

        SaveCallback saveImageCallback = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("flyrt", "done with image save.");
                if (e != null) {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        Log.d("flyrt", "error: object not found");

                        UserInfo.hasTakenSelfie = false;
                        HomeActivity.profilepic.setImageDrawable(
                                context.getResources().getDrawable(R.drawable.blankprofile));
                        HomeActivity.timerRow.setVisibility(View.INVISIBLE);
                    } else {
                        Log.d("flyrt", "error is "+ e.getCode() + ": " + e.getMessage());
                    }
                }
            }
        };
        getUserInfo().saveInBackground(saveImageCallback);
        getUserInfo().pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("flyrt", "done pinning");
            }
        });
    }

    public void saveLocationToCloud() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) return;
        saveLocationToCloud(lastKnownLocation);
    }

    public void saveLocationToCloud(Location location) {
        ParseGeoPoint pgp = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        getUserInfo().put("location", pgp);
        getUserInfo().saveInBackground(saveLocationCallback);
        getUserInfo().pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("flyrt", "save loc - done pinning");
                Toast.makeText(context, "Updated Location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ParseObject getUserInfo() {
        if (userInfo == null) {
            Log.e("flyrt", "user info was null");
            userInfo = new ParseObject("UserInfo");
            HomeActivity.dataID.setText(userInfo.getObjectId());
            sharedPreferencesEditor.putString("id", userInfo.getObjectId()).apply();
            saveLocationToCloud();
        }
        return userInfo;
    }

    public void deleteInfo() {
        getUserInfo().deleteInBackground();
        getUserInfo().unpinInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
//                Log.d("flyrt", "delete info - done unpinning");
            }
        });
        sharedPreferencesEditor.remove("id").apply();
        hasTakenSelfie = false;
    }

    static SaveCallback saveLocationCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            Log.d("flyrt", "done with location save.");
            if (e != null) {
                HomeActivity.resetUI();
                Toast.makeText(context, "Save Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("flyrt", "error is " + e.getCode() + ": " + e.getMessage());
            } else {
                lastLocationUpdateTime = System.currentTimeMillis();
            }

        }
    };
}
