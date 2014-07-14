package com.tbse.flyrt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

public class HomeActivity extends ActionBarActivity {

    ImageView profilepic;
    Button selfieButton;
    Button whosNearbyButton;
    Button cancelButton;
    TextView timer;
    TextView dataID;
    CountDownTimer countDownTimer;
    LinearLayout timerRow;
    Bitmap imageBitmap;

    ParseObject userInfo;
    Criteria criteria;
    // Acquire a reference to the system Location Manager
    LocationManager locationManager;
    Location lastKnownLocation;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    long lastLocationUpdateTime = 0L;

    boolean hasTakenSelfie;
    boolean wantsNotifications;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("flyrt", "onResume called");

        if (sharedPreferences.contains("id")) {
            String id = sharedPreferences.getString("id", "none");

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("flyrt", "onCreate");
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, "LFcNLM94TXKAzaWwuTHlGMaAMiDvzKAUd5TUjxkO",
                               "zzxxAdVzFmKbn41dekkfkaHAsPTa5Wbg3r4u5IiP");

        ParseAnalytics.trackAppOpened(getIntent());

        setContentView(R.layout.activity_home);

        profilepic = (ImageView) findViewById(R.id.profilepic);
        selfieButton = (Button) findViewById(R.id.take_a_selfie);
        whosNearbyButton = (Button) findViewById(R.id.whos_nearby);
        cancelButton = (Button) findViewById(R.id.cancelbutton);
        timer = (TextView) findViewById(R.id.timer);
        timerRow = (LinearLayout) findViewById(R.id.timerRow);
        dataID = (TextView) findViewById(R.id.dataID);

        sharedPreferences = getSharedPreferences("Flyrt", MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        timerRow.setVisibility(View.INVISIBLE);
        hasTakenSelfie = false;

        // get location criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);


        selfieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                if ( ! sharedPreferences.contains("id")) {
                    userInfo = new ParseObject("UserInfo");
                    sharedPreferencesEditor.putString("id", userInfo.getObjectId()).apply();
                }
            }
        });

        whosNearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasTakenSelfie == false) {
                    Toast.makeText(getApplicationContext(), "Please take a selfie first!", Toast.LENGTH_LONG)
                        .show();
                    return;
                }

                // start looking for nearby people

                // put results in notifications

                wantsNotifications = true;

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                profilepic.setImageDrawable(getResources().getDrawable(R.drawable.blankprofile));
                userInfo.deleteInBackground();
                userInfo.unpinInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("flyrt", "done unpinning");
                    }
                });
                sharedPreferencesEditor.remove("id").apply();
                dataID.setText("Data ID");
                hasTakenSelfie = false;
                timerRow.setVisibility(View.INVISIBLE);
                locationManager.removeUpdates(locationListener);
            }
        });

        // turn on Location Services
        // Get Location Manager and check for GPS & Network location services
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }

        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            ParseGeoPoint pgp = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            userInfo.put("location", pgp);
            userInfo.saveInBackground(saveLocationCallback);
            userInfo.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("flyrt", "done pinning");
                }
            });
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    SaveCallback saveLocationCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            Log.d("flyrt", "done with location save.");
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    Log.d("flyrt", "save loc Obj not found");
                    userInfo = new ParseObject("UserInfo");
                    sharedPreferencesEditor.putString("id", userInfo.getObjectId()).apply();
                    dataID.setText(""+userInfo.getObjectId());
                    ParseGeoPoint pgp = new ParseGeoPoint(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude());
                    userInfo.put("location", pgp);
                    userInfo.saveInBackground(saveLocationCallback);
                    userInfo.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("flyrt", "done pinning");
                        }
                    });
                    hasTakenSelfie = false;
                    profilepic.setImageDrawable(getResources().
                            getDrawable(R.drawable.blankprofile));
                    timerRow.setVisibility(View.INVISIBLE);
                } else {
                    Log.d("flyrt", "error is " + e.getCode() + ": " + e.getMessage());
                }
            } else {
                lastLocationUpdateTime = System.currentTimeMillis();
                dataID.setText("" + userInfo.getObjectId());
            }

        }
    };

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (System.currentTimeMillis() - 2*60*1000 > lastLocationUpdateTime) {
                Log.d("flyrt", "Updating location");
                lastKnownLocation = location;
                ParseGeoPoint pgp = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                userInfo.put("location", pgp);
                userInfo.saveInBackground(saveLocationCallback);
                userInfo.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("flyrt", "done pinning");
                    }
                });
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            // get selfie thumbnail
            imageBitmap = (Bitmap) extras.get("data");
            hasTakenSelfie = true;

            // put selfie thumbnail in ImageView
            profilepic.setImageBitmap(imageBitmap);

            // save image to Parse
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            userInfo.put("image", byteArray);

            SaveCallback saveImageCallback = new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("flyrt", "done with image save.");
                    if (e != null) {
                        if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            Log.d("flyrt", "error: object not found");
                            userInfo = new ParseObject("UserInfo");
                            sharedPreferencesEditor.putString("id", userInfo.getObjectId()).apply();
                            ParseGeoPoint pgp = new ParseGeoPoint(lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude());
                            userInfo.put("location", pgp);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            userInfo.put("image", byteArray);

                            userInfo.saveInBackground();
                            userInfo.pinInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.d("flyrt", "done pinning");
                                }
                            });
                            hasTakenSelfie = false;
                            profilepic.setImageDrawable(getResources().getDrawable(R.drawable.blankprofile));
                            timerRow.setVisibility(View.INVISIBLE);
                        } else {
                            Log.d("flyrt", "error is "+ e.getCode() + ": " + e.getMessage());
                        }
                    }
                }
            };
            userInfo.saveInBackground(saveImageCallback);
            userInfo.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d("flyrt", "done pinning");
                }
            });

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

                    // blank timer text
                    timer.setText("");

                    // blank selfie image
                    profilepic.setImageDrawable(getResources().getDrawable(R.drawable.blankprofile));

                    hasTakenSelfie = false;

                    // remove timer row
                    timerRow.setVisibility(View.INVISIBLE);

                    userInfo.deleteInBackground();
                    userInfo.unpinInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("flyrt", "done unpinning");
                        }
                    });

                    sharedPreferencesEditor.remove("id").apply();

                }
            }.start();

            timerRow.setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("flyrt", "onStop called");
    }
}
