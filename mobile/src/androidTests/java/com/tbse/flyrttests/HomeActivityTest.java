package com.tbse.flyrttests;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.robotium.solo.Solo;
import com.tbse.flyrt.FakeCamera;
import com.tbse.flyrt.HomeActivity;
import com.tbse.flyrt.R;
import com.tbse.flyrt.SelfieBtnOnClickListenerFactory;
import com.tbse.flyrt.UserInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    Solo solo;

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        solo.assertCurrentActivity("wrong activity", HomeActivity.class);
    }

    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    @Test
    public void testSelfieButton() throws Exception {
        Log.d("flyrt", "testing selfie button");
        Intent intent = new Intent(getActivity(), FakeCamera.class);
        Bitmap icon = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(),
                R.drawable.flyrtlogo);

        intent.putExtra("data", icon);

                solo.getView(R.id.take_a_selfie)
                        .setOnClickListener(
                                new SelfieBtnOnClickListenerFactory()
                                        .getOnClickListener(intent,
                                                getActivity()
                                        ));
        solo.clickOnView(solo.getView(R.id.take_a_selfie));
        Log.d("flyrt", "waiting for view ...");
        getInstrumentation().waitForIdleSync();
        Log.d("flyrt", "done waiting for view.");
        solo.assertCurrentActivity("camera wrong act", HomeActivity.class);
        assertEquals(true, UserInfo.hasTakenSelfie);

    }

    @Test
    public void testOnCreate() throws Exception {

    }

    @Test
    public void testResetUI() throws Exception {

    }

    @Test
    public void testOnActivityResult() throws Exception {

    }

    @Test
    public void testOnDestroy() throws Exception {

    }
}