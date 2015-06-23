/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotifystreamer.app.android.justinbriggs.net.spotifystreamer;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import net.justinbriggs.android.musicpreviewer.app.data.MusicContract;
import net.justinbriggs.android.musicpreviewer.app.data.MusicContract.TrackEntry;
import net.justinbriggs.android.musicpreviewer.app.data.MusicProvider;

public class TestProvider extends AndroidTestCase {

    /*
        Check to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {

        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MusicProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MusicProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MusicProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MusicContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MusicContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MusicProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
       This helper function deletes all records from the table using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                TrackEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Music table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicTrackQueries() {

        ContentValues testValues = TestUtilities.createTrackValues();
        TestUtilities.insertTrackValues(mContext);

        // Test the basic content provider query
        Cursor trackCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicTrackQueries, track query", trackCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    trackCursor.getNotificationUri(), TrackEntry.CONTENT_URI);
        }
    }


        /*
        This test doesn't touch the database.  It verifies that the ContentProvider returns
        the correct type for each type of URI that it can handle.
         */
    public void testGetType() {

        // content://net.justinbriggs.android.musicpreviewer.app/track/
        String type = mContext.getContentResolver().getType(TrackEntry.CONTENT_URI);

        // vnd.android.cursor.dir/net.justinbriggs.android.musicpreviewer.app/tracks
        assertEquals("Error: the TrackEntry CONTENT_URI should return TrackEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_TYPE, type);

    }


    /*
        This test uses the provider to insert and then update the data.
     */
    public void testUpdateTrack() {

        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createTrackValues();

        Uri locationUri = mContext.getContentResolver().insert(TrackEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(TrackEntry._ID, locationRowId);
        updatedValues.put(TrackEntry.COLUMN_ALBUM_NAME, "Thrust");
        updatedValues.put(TrackEntry.COLUMN_ARTIST_NAME, "Herbie Hancock");
        updatedValues.put(TrackEntry.COLUMN_TRACK_NAME, "Actual Proof");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(TrackEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                TrackEntry.CONTENT_URI, updatedValues, TrackEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,   // projection
                TrackEntry._ID + " = " + locationRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateTrack.  Error validating track entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    //TODO: Finish out any necessary tests.


//    // Make sure we can still delete after adding/updating stuff
//    //
//    // Student: Uncomment this test after you have completed writing the insert functionality
//    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
//    // query functionality must also be complete before this test can be used.
//    public void testInsertReadProvider() {
//        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
//
//        // Register a content observer for our insert.  This time, directly with the content resolver
//        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, tco);
//        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
//
//        // Did our content observer get called?  Students:  If this fails, your insert location
//        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//        mContext.getContentResolver().unregisterContentObserver(tco);
//
//        long locationRowId = ContentUris.parseId(locationUri);
//
//        // Verify we got a row back.
//        assertTrue(locationRowId != -1);
//
//        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
//        // the round trip.
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                LocationEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//
//        TestUtilities.validateCursor("testInsertReadProvider. Error validating LocationEntry.",
//                cursor, testValues);
//
//        // Fantastic.  Now that we have a location, add some weather!
//        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
//        // The TestContentObserver is a one-shot class
//        tco = TestUtilities.getTestContentObserver();
//
//        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, tco);
//
//        Uri weatherInsertUri = mContext.getContentResolver()
//                .insert(WeatherEntry.CONTENT_URI, weatherValues);
//        assertTrue(weatherInsertUri != null);
//
//        // Did our content observer get called?  Students:  If this fails, your insert weather
//        // in your ContentProvider isn't calling
//        // getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//        mContext.getContentResolver().unregisterContentObserver(tco);
//
//        // A cursor is your primary interface to the query results.
//        Cursor weatherCursor = mContext.getContentResolver().query(
//                WeatherEntry.CONTENT_URI,  // Table to Query
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null // columns to group by
//        );
//
//        TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
//                weatherCursor, weatherValues);
//
//        // Add the location values in with the weather data so that we can make
//        // sure that the join worked and we actually get all the values back
//        weatherValues.putAll(testValues);
//
//        // Get the joined Weather and Location data
//        weatherCursor = mContext.getContentResolver().query(
//                WeatherEntry.buildWeatherLocation(TestUtilities.TEST_LOCATION),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data.",
//                weatherCursor, weatherValues);
//
//        // Get the joined Weather and Location data with a start date
//        weatherCursor = mContext.getContentResolver().query(
//                WeatherEntry.buildWeatherLocationWithStartDate(
//                        TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data with start date.",
//                weatherCursor, weatherValues);
//
//        // Get the joined Weather data for a specific date
//        weatherCursor = mContext.getContentResolver().query(
//                WeatherEntry.buildWeatherLocationWithDate(TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
//                null,
//                null,
//                null,
//                null
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location data for a specific date.",
//                weatherCursor, weatherValues);
//    }
//
//    // Make sure we can still delete after adding/updating stuff
//    //
//    // Student: Uncomment this test after you have completed writing the delete functionality
//    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
//    // query functionality must also be complete before this test can be used.
//    public void testDeleteRecords() {
//        testInsertReadProvider();
//
//        // Register a content observer for our location delete.
//        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, locationObserver);
//
//        // Register a content observer for our weather delete.
//        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);
//
//        deleteAllRecordsFromProvider();
//
//        // Students: If either of these fail, you most-likely are not calling the
//        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
//        // delete.  (only if the insertReadProvider is succeeding)
//        locationObserver.waitForNotificationOrFail();
//        weatherObserver.waitForNotificationOrFail();
//
//        mContext.getContentResolver().unregisterContentObserver(locationObserver);
//        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
//    }
//
//
    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertTrackValues(long locationRowId) {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues trackValues = new ContentValues();

            trackValues.put(MusicContract.TrackEntry.COLUMN_ALBUM_NAME, "Album Name " + i);
            trackValues.put(MusicContract.TrackEntry.COLUMN_TRACK_NAME, "Track Name " + i);
            trackValues.put(MusicContract.TrackEntry.COLUMN_ARTIST_NAME, "Artist Name " + i);
            returnContentValues[i] = trackValues;
        }
        return returnContentValues;
    }
//
        // Note that this test will work with the built-in (default) provider
//    // implementation, which just inserts records one-at-a-time, so really do implement the
//    // BulkInsert ContentProvider function.
    public void testBulkInsert() {

        // first, let's create a track value
        ContentValues testValues = TestUtilities.createTrackValues();
        Uri trackUri = mContext.getContentResolver().insert(TrackEntry.CONTENT_URI, testValues);
        long trackRowId = ContentUris.parseId(trackUri);

        // Verify we got a row back.
        assertTrue(trackRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating TrackEntry.",
                cursor, testValues);

        // Now we can bulkInsert some tracks.  In fact, we only implement BulkInsert for track
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertTrackValues(trackRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // Sort order
        );

        // we should have as many records in the database as we've inserted
        //TODO: This breaks because there is still a record from the first insert in this method.
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT + 1);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating TrackEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
