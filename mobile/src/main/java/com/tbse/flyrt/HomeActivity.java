package com.tbse.flyrt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
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
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;

import java.io.ByteArrayOutputStream;




public class HomeActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static ImageView profilepic;
    public static TextView timer;
    public static TextView dataID;
    public static LinearLayout timerRow;
    public static Drawable blankProfile;
    Button selfieButton;
    Button whosNearbyButton;
    Button cancelButton;
    CountDownTimer countDownTimer;
    Bitmap imageBitmap;
    UserInfo userInfo;
    // Acquire a reference to the system Location Manager
    Criteria criteria;
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
                    userInfo.saveLocationToCloud(location);
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

        if (! alreadyEnabledParseLocalDatastore) {
            Parse.enableLocalDatastore(getApplicationContext());
            Parse.initialize(getApplicationContext(), "LFcNLM94TXKAzaWwuTHlGMaAMiDvzKAUd5TUjxkO",
                "zzxxAdVzFmKbn41dekkfkaHAsPTa5Wbg3r4u5IiP");

            ParseAnalytics.trackAppOpened(getIntent());
            alreadyEnabledParseLocalDatastore = true;
        }

        setContentView(R.layout.activity_home);

        userInfo = new UserInfo(getApplicationContext());
        userInfo.init();

        profilepic = (ImageView) findViewById(R.id.profilepic);
        blankProfile = getResources().getDrawable(R.drawable.blankprofile);
        selfieButton = (Button) findViewById(R.id.take_a_selfie);
        whosNearbyButton = (Button) findViewById(R.id.whos_nearby);
        cancelButton = (Button) findViewById(R.id.cancelbutton);
        timer = (TextView) findViewById(R.id.timer);
        timerRow = (LinearLayout) findViewById(R.id.timerRow);
        dataID = (TextView) findViewById(R.id.dataID);

        timerRow.setVisibility(View.INVISIBLE);

        // get location criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        selfieButton.setOnClickListener(new SelfieBtnOnClickListenerFactory().getOnClickListener(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                getLocationManager(),
                getLocationListener(),
                this
        ));

        whosNearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (UserInfo.hasTakenSelfie == false) {
                    Toast.makeText(getApplicationContext(),
                            "Please take a selfie first!",
                            Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                // start looking for nearby people

                // put results in notifications

                UserInfo.wantsNotifications = true;

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();

                resetUI();
                userInfo.deleteInfo();


                getLocationManager().removeUpdates(locationListener);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("flyrt", "on act result, code: " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("flyrt", "calling set selfie");
            setImageAfterSelfie(data);
        }
    }

    private LocationManager getLocationManager() {
        return locationManager;
    }

    private void setImageAfterSelfie(Intent data) {

        Bundle extras = data.getExtras();

        // get selfie thumbnail
        imageBitmap = (Bitmap) extras.get("data");
        Log.d("flyrt", "setting image after selfie");
        UserInfo.hasTakenSelfie = true;

        // put selfie thumbnail in ImageView
        profilepic.setImageBitmap(imageBitmap);

        // save image to Parse
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        userInfo.uploadSelfie(byteArray);

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
                userInfo.deleteInfo();
            }
        }.start();

        timerRow.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onDestroy() {
        resetUI();
        userInfo.deleteInfo();
        super.onDestroy();
    }
}
