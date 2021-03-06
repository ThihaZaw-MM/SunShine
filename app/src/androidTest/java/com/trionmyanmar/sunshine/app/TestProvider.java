package com.trionmyanmar.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.trionmyanmar.sunshine.app.data.WeatherContract.LocationEntry;
import com.trionmyanmar.sunshine.app.data.WeatherContract.WeatherEntry;
import com.trionmyanmar.sunshine.app.data.WeatherDbHelper;

/**
 * Created by thiha on 9/1/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase("");
    }

    public void testInsertReadProvider() {
        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;
        Uri locationInsertUris = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values);
        locationRowId = ContentUris.parseId(locationInsertUris);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        assertTrue(locationInsertUris != null);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex((LocationEntry.COLUMN_CITY_NAME));
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LAT));
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LONG));
            double longitude = cursor.getDouble(longIndex);

            // Hooray, data was returned!  Assert that it's the right data, and that the database
            // creation code is working as intended.
            // Then take a break.  We both know that wasn't easy.
            assertEquals(testCityName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

            // Fantastic.  Now that we have a location, add some weather!
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
            weatherValues.put(WeatherEntry.COLUMN_DATE, "20141205");
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

            //dbHelper.close();

            Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
            long weatherRowId = ContentUris.parseId(weatherInsertUri);
            assertTrue(weatherInsertUri != null);

            // A cursor is your primary interface to the query results.
            Cursor weatherCursor = db.query(
                    WeatherEntry.TABLE_NAME,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null  // sort order
            );

            if (!weatherCursor.moveToFirst()) {
                fail("No weather data returned!");
            }

            assertEquals(weatherCursor.getInt(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY)), locationRowId);
            assertEquals(weatherCursor.getString(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DATE)), "20141205");
            assertEquals(weatherCursor.getDouble(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_DEGREES)), 1.1);
            assertEquals(weatherCursor.getDouble(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY)), 1.2);
            assertEquals(weatherCursor.getDouble(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE)), 1.3);
            assertEquals(weatherCursor.getInt(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), 75);
            assertEquals(weatherCursor.getInt(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), 65);
            assertEquals(weatherCursor.getString(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC)), "Asteroids");
            assertEquals(weatherCursor.getDouble(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED)), 5.5);
            assertEquals(weatherCursor.getInt(
                    weatherCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID)), 321);

            weatherCursor.close();
            dbHelper.close();
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }
    }

    public void testGetType() {
                // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
                // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
                // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
        WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
        WeatherEntry.buildWeatherLocationWithDate(testLocation, 0));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
        }

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    /*public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDb.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, updatedValues);
    }*/

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }
}