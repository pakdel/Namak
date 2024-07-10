package com.amirpakdel.namak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class NamakApplicationTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void testInitialization() {

//        PackageInfo info = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
//        assertNotNull(info);
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
//        assertEquals(application.getString(R.string.not_logged_in), sm.getName());
    }
}