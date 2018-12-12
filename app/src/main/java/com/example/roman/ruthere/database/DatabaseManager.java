package com.example.roman.ruthere.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "database";
    private static final String CHECKED_IN_TABLE = "checkedIn";
    private static final String TIME = "time";
    private static final String DATE = "date";
    private static final String DEVICE_ID = "device_id";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE   = "longitude";
    private static final String NAME = "address";
    private static final String ZIP = "zip";
    private static final String CURRENT = "current";

    public DatabaseManager(Context context) {
        super(context,DATABASE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOCATION_TABLE = "CREATE TABLE "+CHECKED_IN_TABLE+" ("+
                DEVICE_ID+" TEXT,"+
                TIME+" TEXT,"+
                DATE+" TEXT,"+
                LATITUDE+" TEXT,"+
                LONGITUDE+" TEXT,"+
                NAME+" TEXT,"+
                ZIP+" TEXT, "+
                CURRENT+" BOOLEAN, "+
                "PRIMARY KEY ("+DEVICE_ID+","+TIME+","+DATE+"))";
        db.execSQL(CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void checkIn(String deviceID, String time, String date, String latitude, String longitude, String name, String zip){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        // 3. ADD THE VALUES
        values.put(DEVICE_ID, deviceID);
        values.put(TIME, time);
        values.put(DATE, date);
        values.put(LATITUDE, latitude);
        values.put(LONGITUDE, longitude);
        values.put(NAME, name);
        values.put(ZIP,zip);
        values.put(CURRENT, true);
        // 4. INSERT VALUES TO TABLE
        db.insert(CHECKED_IN_TABLE,null,values);
        // 5. CLOSE DB
        db.close();
    }

    public int getPlaceCount(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT COUNT(*) FROM " + CHECKED_IN_TABLE + " WHERE " + NAME + " = ? AND " + CURRENT + " <> ?";
        String value = "0";
        Cursor cursor = db.rawQuery(query,new String[]{name,value});
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex("COUNT(*)"));
        return count;
    }

    public List<String> getCurrentCrowd(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT "+LATITUDE+", "+LONGITUDE+", COUNT(*) FROM "+CHECKED_IN_TABLE+" WHERE "+CURRENT+" <> 0 GROUP BY "+LATITUDE+", "+LONGITUDE;
        Cursor cursor = db.rawQuery(query,null);
        List<String> locations = new ArrayList<>();
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                String latitude = cursor.getString(cursor.getColumnIndex(LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndex(LONGITUDE));
                int count = cursor.getInt(cursor.getColumnIndex("COUNT(*)"));
                locations.add(latitude+","+longitude+","+count);
                cursor.moveToNext();
            }
        }
        return locations;
    }
    // FOR DEBUGGING
    public List<String> getstatus(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT "+DEVICE_ID+", "+NAME+", "+LATITUDE+", "+LONGITUDE+", "+CURRENT+" FROM "+CHECKED_IN_TABLE;
        Cursor cursor = db.rawQuery(query,null);
        List<String> locations = new ArrayList<>();
        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                String id = cursor.getString(cursor.getColumnIndex(DEVICE_ID));
                String name = cursor.getString(cursor.getColumnIndex(NAME));
                String latitude = cursor.getString(cursor.getColumnIndex(LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndex(LONGITUDE));
                String current = cursor.getString(cursor.getColumnIndex(CURRENT));
                locations.add(id+", "+name+", "+latitude+", "+longitude+", "+current);
                cursor.moveToNext();
            }
        }
        return locations;
    }
    public void checkOut(String deviceID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CURRENT,false);
        db.update(CHECKED_IN_TABLE,cv,DEVICE_ID+" = '"+deviceID+"'",null);
    }
}
