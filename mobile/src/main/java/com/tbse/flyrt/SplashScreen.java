package com.tbse.flyrt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {

    public static int TIMER = 2000;

    @Override
    protected void onCreate(Bundle save) {

        super.onCreate(save);

        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(
                new Runnable() {

                    public void run() {

                        Intent i = new Intent(SplashScreen.this, HomeActivity.class);
                        startActivity(i);
                        finish();


                    }



                },
                TIMER);



    }


}