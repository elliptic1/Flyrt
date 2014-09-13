package com.tbse.flyrt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;


public class HomeActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static ImageView profilepic;
    public static TextView timer;
    public static TextView dataID;
    public static LinearLayout timerRow;
    public static Drawable blankProfile;
    Button selfieButton;
    // Button whosNearbyButton;
    Button cancelButton;
    CountDownTimer countDownTimer;
    Bitmap imageBitmap;
    // Acquire a reference to the system Location Manager
    // Criteria criteria;

    // Define a listener that responds to location updates
    LocationManager locationManager;

    private LocationListener locationListener = null;

    public static void resetUI() {
        timer.setText("");
        profilepic.setImageDrawable(blankProfile);
        timerRow.setVisibility(View.INVISIBLE);
        dataID.setText("Data ID");
    }

    private LocationListener getLocationListener() {
        if (locationListener != null) return locationListener;

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (System.currentTimeMillis() - 2 * 60 * 1000 > UserInfo.lastLocationUpdateTime) {
                    Log.d("flyrt", "Updating location");
                    UserInfo.saveLocationToCloud(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        return locationListener;
    }

    private static boolean alreadyEnabledParseLocalDatastore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        if (!alreadyEnabledParseLocalDatastore) {
            Parse.enableLocalDatastore(getApplicationContext());
            alreadyEnabledParseLocalDatastore = true;
        }
        Parse.initialize(getApplicationContext(),
                getString(R.string.parse_app_id),
                getString(R.string.parse_client_key));

        ParseAnalytics.trackAppOpened(getIntent());

        // Associate the device with a user
        ParseUser.enableAutomaticUser();
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();

        getLocationManager().requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, getLocationListener());

        setContentView(R.layout.activity_home);

        profilepic = (ImageView) findViewById(R.id.profilepic);
        blankProfile = getResources().getDrawable(R.drawable.blankprofile);
        selfieButton = (Button) findViewById(R.id.take_a_selfie);
        cancelButton = (Button) findViewById(R.id.cancelbutton);
        timer = (TextView) findViewById(R.id.timer);
        timerRow = (LinearLayout) findViewById(R.id.timerRow);
        dataID = (TextView) findViewById(R.id.dataID);
        dataID.setText("id: " + ParseUser.getCurrentUser().getObjectId());

        timerRow.setVisibility(View.INVISIBLE);

        // get location criteria
//        criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(true);
//        criteria.setPowerRequirement(Criteria.POWER_LOW);

        selfieButton.setOnClickListener(new SelfieBtnOnClickListenerFactory().getOnClickListener(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                this
        ));

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();

                resetUI();
                ParseUser.getCurrentUser().logOut();

                getLocationManager().removeUpdates(locationListener);
            }
        });

    }

    boolean hasTakenSelfie() {
        return profilepic.getDrawable() != getResources().getDrawable(R.drawable.blankprofile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setImageAfterSelfie(data);
            dataID.setText("id: " + ParseUser.getCurrentUser().getObjectId());
        }
    }

    private LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
        if (locationManager == null) Log.e("flyrt", "LM is still null");
        return locationManager;
    }

    private void setImageAfterSelfie(Intent data) {

        Bundle extras = data.getExtras();

        // get selfie thumbnail
        imageBitmap = (Bitmap) extras.get("data");

        // put selfie thumbnail in ImageView
        profilepic.setImageBitmap(imageBitmap);

        // save image to Parse
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        UserInfo.uploadSelfie(byteArray);

        // start timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(15 * 60 * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText(getString(R.string.minutes_left) + millisUntilFinished / 1000 / 60);

                // every 30 seconds
                // search for nearby users

            }

            public void onFinish() {
                resetUI();
                ParseUser.logOut();
            }
        }.start();

        timerRow.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onDestroy() {
        ParseUser.logOut();
        super.onDestroy();
    }
}
