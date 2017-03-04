package info.location.save.sqldatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import info.location.save.MainActivity;

/**
 * Created by ashrafiqubal on 05/01/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DataBase Handler";
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "SaveLocation";

    // Location table name
    private static final String TABLE_ALL_LOCATIONS = "LOCATIONS";

    // Location Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_ADDRESS_TITLE = "address_title";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ALL_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LATITUDE + " TEXT," + KEY_LONGITUDE + " TEXT,"
                + KEY_ADDRESS_TITLE + " TEXT,"+ KEY_ADDRESS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_LOCATIONS);

        // Create tables again
        onCreate(db);
    }

    // Adding new Location
    public void addLocation(Double latitude, Double longitude, String address_title, String address){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE,latitude); // Latitude
        values.put(KEY_LONGITUDE,longitude); //Longitude
        values.put(KEY_ADDRESS_TITLE,address_title);//address title
        values.put(KEY_ADDRESS,address); //Address

        // Inserting Row
        db.insert(TABLE_ALL_LOCATIONS,null,values);
        db.close(); // Closing database connection
        Log.d("DataBaseHandler: ","Location Saved - "+latitude+" "+longitude+" "+address_title+ " "+address);
    }

    // Getting single Location
    public String[] getLocation(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ALL_LOCATIONS, new String[] { KEY_ID,
                KEY_LATITUDE, KEY_LONGITUDE,KEY_ADDRESS_TITLE, KEY_ADDRESS }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        String[] locationDetails = new String[2];
        if (cursor != null)
            cursor.moveToFirst();
        locationDetails[0]=cursor.getString(1);
        locationDetails[1]=cursor.getString(2);

        //Log.d(TAG,cursor.getDouble(0)+" "+cursor.getDouble(1)+" "+cursor.getDouble(2)+" "+cursor.getString(3)+" "+cursor.getString(4));
        return locationDetails;
    }

    // Getting All Location
    public void getAllLocation(){
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ALL_LOCATIONS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        int i=getLocationsCount();
        i--;
        if (cursor.moveToFirst()) {
            do {
                MainActivity.idSet[i]=cursor.getInt(0);
                MainActivity.addressSet[i]=cursor.getString(4);
                MainActivity.titleSet[i]=cursor.getString(3);
                MainActivity.longitudeSet[i]=cursor.getString(2);
                MainActivity.latitudeSet[i]=cursor.getString(1);
                Log.d(TAG,cursor.getInt(0)+","+cursor.getString(1)+","+cursor.getString(2)+","+cursor.getString(3)+","+cursor.getString(4));
                Log.d(TAG,MainActivity.titleSet[i]+MainActivity.addressSet[i]+MainActivity.idSet[i]);
                i--;
                } while (cursor.moveToNext());
        }
    }

    // Deleting single Location
    public void deleteLocation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALL_LOCATIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }
    // Getting Locations Count
    public int getLocationsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ALL_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int len = cursor.getCount();
        cursor.close();

        // return count
        return len;
    }
}
