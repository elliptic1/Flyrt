package com.tbse.flyrttests;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

    public Uri setImageUri() {
        // Store image in dcim
        Resources resources = getActivity().getResources();
        return Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(R.id.logo) + '/'
                + resources.getResourceTypeName(R.id.logo) + '/'
                + resources.getResourceEntryName(R.id.logo)
        );
    }

    @Test
    public void testSelfieButton() throws Exception {
        Log.d("flyrt", "testing selfie button");
        Intent intent = new Intent(getActivity(), FakeCamera.class);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
        solo.getView(R.id.take_a_selfie)
                .setOnClickListener(
                        new SelfieBtnOnClickListenerFactory()
                                .getOnClickListener(intent,
                                        (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE),
                                        getLocationListenerStub(),
                                        getActivity()
                                ));
        solo.clickOnView(solo.getView(R.id.take_a_selfie));
        Log.d("flyrt", "waiting for view ...");
        getInstrumentation().waitForIdleSync();
        Log.d("flyrt", "done waiting for view.");
        solo.assertCurrentActivity("camera wrong act", HomeActivity.class);
        assertEquals(true, UserInfo.hasTakenSelfie);

    }

    private LocationListener getLocationListenerStub() {

        return new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.d("flyrt", "test - onLocChanged");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
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