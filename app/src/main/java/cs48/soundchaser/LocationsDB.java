package cs48.soundchaser;

/**
 * Created by Krassi on 2/25/2016.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationsDB extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "locationManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "locations";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_LIST = "list";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_DURATION = "duration";

    public LocationsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "create table "
                + TABLE_CONTACTS
                + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY , "
                + KEY_TIME + " TEXT , "
                + KEY_LIST + " TEXT , "
                + KEY_DISTANCE + " TEXT , "
                + KEY_DURATION + " TEXT "
                + " ) ";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    void addLocations(LocationList locList) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, locList.getTime());
        values.put(KEY_LIST, locList.getList());
        values.put(KEY_DISTANCE, locList.getDistance());
        values.put(KEY_DURATION, locList.getDuration());

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    LocationList getLocations(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID,
                        KEY_TIME, KEY_LIST, KEY_DISTANCE, KEY_DURATION}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        LocationList locList = new LocationList(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        db.close();
        cursor.close();
        // return contact
        return locList;
    }

    // Getting All Contacts
    public List<LocationList> getAllLocations() {
        List<LocationList> locList = new ArrayList<LocationList>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LocationList locationList = new LocationList();
                locationList.setId(Integer.parseInt(cursor.getString(0)));
                locationList.setTime(cursor.getString(1));
                locationList.setList(cursor.getString(2));
                locationList.setDistance(cursor.getString(3));
                locationList.setDuration(cursor.getString(4));
                // Adding contact to list
                locList.add(locationList);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        // return contact list
        return locList;
    }

    // Deleting single contact
    public void deleteLocation(LocationList locList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(locList.getId()) });
        db.close();
    }

    // Getting contacts Count
    public int getLocationCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int c = cursor.getCount();
        cursor.close();
        db.close();

        // return count
        return c;
    }

}