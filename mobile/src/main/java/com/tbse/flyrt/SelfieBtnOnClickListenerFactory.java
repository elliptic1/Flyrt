package com.tbse.flyrt;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

/**
 * Created by tbsmith on 8/19/14.
 */
public class SelfieBtnOnClickListenerFactory {

    public SelfieBtnOnClickListenerFactory() {
    }

    public View.OnClickListener getOnClickListener(final Intent intent,
                                                   final Activity activity
    ) {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("flyrt", "button clicked, dispatching");
                dispatchTakePictureIntent(intent);
                Log.d("flyrt", "req loc updates");
            }


            private void dispatchTakePictureIntent(Intent intent) {

                // Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Log.d("flyrt", "checking resolve activity");
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    Log.d("flyrt", "starting act for result");
                    activity.startActivityForResult(intent, 1);
                }
            }

        };

    } // end method
}

