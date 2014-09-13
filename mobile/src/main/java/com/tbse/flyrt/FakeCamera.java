package com.tbse.flyrt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class FakeCamera extends Activity {

    private final static String TAG = "flyrt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("flyrt", "starting FakeCamera");
        prepareToSnapPicture();
        Log.d("flyrt", "finishing FakeCamera");
        finish();
    }

    private void prepareToSnapPicture() {
        checkSdCard();
        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            setResult(RESULT_OK, intent);
        } else {
            Log.i(TAG, "Unable to capture photo. Missing Intent Extras.");
            setResult(RESULT_CANCELED, intent);
        }
    }

    private void checkSdCard() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "External SD card not mounted", Toast.LENGTH_LONG).show();
            Log.i(TAG, "External SD card not mounted");
        }
    }

}



