package com.amirpakdel.namak;

import android.content.pm.PackageInfo;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 *
 * https://developer.android.com/tools/testing/index.html
 *
 * http://developer.android.com/tools/testing/activity_testing.html
 * https://developer.android.com/training/activity-testing/activity-unit-testing.html
 * https://developer.android.com/training/activity-testing/activity-basic-testing.html
 * https://developer.android.com/training/testing.html
 *
 * http://www.vogella.com/tutorials/AndroidTesting/article.html
 *
 */
public class ApplicationTest extends ApplicationTestCase<NamakApplication> {

    private NamakApplication application;
    public ApplicationTest() {
        super(NamakApplication.class);
    }


    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        application = getApplication();
    }

    public void testInitialization() throws Exception {
        PackageInfo info = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
        assertNotNull(info);
//        MoreAsserts.assertMatchesRegex("\\d\\.\\d alpha \\d", info.versionName);
        assertNotNull(NamakApplication.getAppContext());
        assertNotNull(NamakApplication.getPref());
        assertNotNull(NamakApplication.getDashboardAdapter());
//        assertNotNull(NamakApplication.getAutoExecute());
        assertEquals(-1, NamakApplication.getSaltMasterIndex());

        SaltMaster sm = NamakApplication.getSaltMaster();
        assertNotNull(sm);
        assertNull(sm.getAuthToken());
        assertNull(sm.getBaseUrl());
        assertNull(sm.getId());
        assertEquals(application.getString(R.string.not_logged_in), sm.getName());
    }
}