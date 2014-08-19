package com.tbse.flyrttests;

import android.test.AndroidTestCase;

import com.parse.ParseObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserInfoTest extends AndroidTestCase {

    public static ParseObject userInfo;

    @Before
    public void setUp() throws Exception {
        userInfo = new ParseObject("UserInfo");
    }

    @After
    public void tearDown() throws Exception {
        userInfo = null;
    }

    @Test
    public void testInit() throws Exception {

    }

    @Test
    public void testUploadSelfie() throws Exception {

    }

    @Test
    public void testSaveLocationToCloud() throws Exception {

    }

    @Test
    public void testSaveLocationToCloud1() throws Exception {

    }

    @Test
    public void testGetUserInfo() throws Exception {

    }

    @Test
    public void testDeleteInfo() throws Exception {

    }
}