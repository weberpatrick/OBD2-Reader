package obd2.dhbw.de.obd2_reader.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import obd2.dhbw.de.obd2_reader.container.DataRow;
import obd2.dhbw.de.obd2_reader.container.TripRow;

/**
 * Created by Ricardo on 02.04.2016.
 */
public class DbHelper
       extends SQLiteOpenHelper
{
//    http://www.programmierenlernenhq.de/daten-in-sqlite-datenbank-schreiben-und-lesen-in-android/

//    create table stuff
//    https://www.sqlite.org/lang_createtable.html

//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private static final String LOG_TAG = DbHelper.class.getName();

//  database
    private static final int DATABASE_VERSION   = 1;
    public static final String DATABASE_NAME    = "obdDB.db";
    public static final String TABLE_CAR_DATA   = "carData";
    public static final String TABLE_TRIP       = "trip";

//	***************************************************************************
//	columns
//	***************************************************************************

//  car data columns
    private static final String C_ID                         = TABLE_CAR_DATA + "_" + "id";
    private static final String C_TIMESTAMP                  = TABLE_CAR_DATA + "_" + "timestamp";
    private static final String C_ENGINE_LOAD                = TABLE_CAR_DATA + "_" + "engineLoad";
    private static final String C_RPM                        = TABLE_CAR_DATA + "_" + "rpm";
    private static final String C_SPEED                      = TABLE_CAR_DATA + "_" + "speed";
    private static final String C_THROTTLE_POSITION          = TABLE_CAR_DATA + "_" + "throttlePosition";
    private static final String C_RUNTIME                    = TABLE_CAR_DATA + "_" + "runTime";

//  TODO check whether a foreign key connection is necessary
    private static final String C_TRIP_ID                    = TABLE_CAR_DATA + "_" + "tripId";
    private static final String C_GPS_SPEED                  = TABLE_CAR_DATA + "_" + "gpsSpeed";
    private static final String C_LATITUDE                   = TABLE_CAR_DATA + "_" + "latitude";
    private static final String C_LONGITUDE                  = TABLE_CAR_DATA + "_" + "longitude";
    private static final String C_ALTITUDE                   = TABLE_CAR_DATA + "_" + "altitude";

//  trip columns
    private static final String T_ID                        = TABLE_TRIP + "_" + "id";
    private static final String T_DATE                      = TABLE_TRIP + "_" + "date";
    private static final String T_TRACK_LENGTH              = TABLE_TRIP + "_" + "trackLength";
    private static final String T_DRIVING_TIME              = TABLE_TRIP + "_" + "drivingTime";
    private static final String T_STAND_TIME                = TABLE_TRIP + "_" + "standTime";
    private static final String T_MAX_SPEED                 = TABLE_TRIP + "_" + "maxSpeed";
    private static final String T_AVG_SPEED                 = TABLE_TRIP + "_" + "avgSpeed";
    private static final String T_NAME                      = TABLE_TRIP + "_" + "name";

//  sql commands
    private static final String TABLE_CAR_DATA_CREATE =
            "CREATE TABLE " + TABLE_CAR_DATA
                    + " ("
                    + C_ID                      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + C_TIMESTAMP               + " TEXT DEFAULT CURRENT_TIMESTAMP,"
                    + C_ENGINE_LOAD             + " REAL,"
                    + C_RPM                     + " INTEGER,"
                    + C_SPEED                   + " INTEGER,"
                    + C_THROTTLE_POSITION       + " REAL,"
                    + C_RUNTIME                 + " INTEGER,"
                    + C_TRIP_ID                 + " INTEGER,"
                    + C_GPS_SPEED               + " REAL,"
                    + C_LATITUDE                + " REAL,"
                    + C_LONGITUDE               + " REAL,"
                    + C_ALTITUDE                + " REAL"
                    + ");";

    private static final String TABLE_TRIP_CREATE =
            "CREATE TABLE " + TABLE_TRIP
                    + " ("
                    + T_ID                      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + T_DATE                    + " TEXT,"
                    + T_TRACK_LENGTH            + " REAL,"
                    + T_DRIVING_TIME            + " INTEGER,"
                    + T_STAND_TIME              + " INTEGER,"
                    + T_MAX_SPEED               + " INTEGER,"
                    + T_AVG_SPEED               + " REAL,"
                    + T_NAME                    + " TEXT DEFAULT Standard"
                    + ");";

//	***************************************************************************
//	CONSTRUCTOR
//	***************************************************************************

    public DbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            Log.d(LOG_TAG, "Create the car data table with: " + TABLE_CAR_DATA_CREATE);
            db.execSQL(TABLE_CAR_DATA_CREATE);

            Log.d(LOG_TAG, "Create the trip table with: " + TABLE_TRIP_CREATE);
            db.execSQL(TABLE_TRIP_CREATE);
        }
        catch (SQLException e)
        {
            Log.e(LOG_TAG, "Error while creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    public boolean insertCarData( double engineLoad
                                , int rpm
                                , int speed
                                , double throttlePosition
                                , int runTime
                                , int tripId
                                , double gpsSpeed
                                , double latitude
                                , double longitude
                                , double altitude
                                )
    {
        ContentValues values = new ContentValues();
        values.put(C_ENGINE_LOAD, engineLoad);
        values.put(C_RPM, rpm);
        values.put(C_SPEED, speed);
        values.put(C_THROTTLE_POSITION, throttlePosition);
        values.put(C_RUNTIME, runTime);
        values.put(C_TRIP_ID, tripId);
        values.put(C_GPS_SPEED, gpsSpeed);
        values.put(C_LATITUDE, latitude);
        values.put(C_LONGITUDE, longitude);
        values.put(C_ALTITUDE, altitude);

        SQLiteDatabase db = this.getWritableDatabase();
        long insertId = db.insert(TABLE_CAR_DATA, null, values);

//      insertID = -1 means the insertCarData failed
        if(insertId == -1) return false;

        return true;
    }

    public DataRow selectCarData(int tripId)
    {
        SQLiteDatabase db = this.getReadableDatabase();

//      declare cursor to read data
//      selectCarData row with highest id
        Cursor cursor = db.query( TABLE_CAR_DATA
                                , null //columns
                                , C_TRIP_ID + "=?" //where clause
                                , new String[] {String.valueOf(tripId)} //selectionArgs
                                , null //groupBy
                                , null //having
                                , C_ID + " DESC" //order by
                                , "1" //limit
                                );

        if(cursor.moveToPosition(0))
        {
            return new DataRow( cursor.getInt(0)
                              , cursor.getString(1)
                              , cursor.getDouble(2)
                              , cursor.getInt(3)
                              , cursor.getInt(4)
                              , cursor.getDouble(5)
                              , cursor.getInt(6)
                              , cursor.getInt(7)
                              , cursor.getDouble(8)
                              , cursor.getDouble(9)
                              , cursor.getDouble(10)
                              , cursor.getDouble(11)
                              );
        }

        return null;
    }

    public int getLatestTripId()
    {
        SQLiteDatabase db = this.getReadableDatabase();

//      declare cursor to read data
//      selectCarData row with highest id
        Cursor cursor = db.query( TABLE_TRIP
                , new String[]{T_ID} //columns
                , null //T_ID +"=1" //where clause
                , null //selectionArgs
                , null //groupBy
                , null //having
                , T_ID + " DESC" //order by
                , "1" //limit
        );

        if(cursor.moveToPosition(0)) return cursor.getInt(0);

        return 0;
    }

    public int[] getTripIds()
    {
        SQLiteDatabase db = this.getReadableDatabase();

//      declare cursor to read data
//      selectCarData row with highest id
        Cursor cursor = db.query( TABLE_TRIP
                , new String[]{T_ID} //columns
                , null //T_ID +"=1" //where clause
                , null //selectionArgs
                , null //groupBy
                , null //having
                , T_ID + " DESC" //order by
                , null //limit
        );

        int[] tripIds = new int[cursor.getCount()];

        for (int i = 0; i < cursor.getCount(); i++){
            if(cursor.moveToPosition(i)){
                tripIds[i] = cursor.getInt(0);
            }
        }

        return tripIds;
    }

    public boolean insertTripData( int tripId
                                 , String date
                                 , double trackLength
                                 , long drivingTime
                                 , long standTime
                                 , double maxSpeed
                                 , double avgSpeed
                                 , String name
                                 )
    {
        ContentValues values = new ContentValues();
        values.put(T_ID, tripId);
        values.put(T_DATE, date);
        values.put(T_TRACK_LENGTH, trackLength);
        values.put(T_DRIVING_TIME, drivingTime);
        values.put(T_STAND_TIME, standTime);
        values.put(T_MAX_SPEED, maxSpeed);
        values.put(T_AVG_SPEED, avgSpeed);
        if(name != null && !name.isEmpty()) values.put(T_NAME, name);

        SQLiteDatabase db = this.getWritableDatabase();
        long insertId = db.insert(TABLE_TRIP, null, values);

//      insertID = -1 means the insertCarData failed
        if(insertId == -1) return false;

        return true;
    }

    public boolean insertTripData(TripRow row)
    {
        return insertTripData( row.getTripId()
                             , row.getDate()
                             , row.getDistance()
                             , row.getRunTime()
                             , row.getStandTime()
                             , row.getMaxSpeed()
                             , row.getAvgSpeed()
                             , row.getName()
        );
    }

    public TripRow selectTrip(int tripId)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query( TABLE_TRIP
                , null //columns
                , T_ID + "=?" //DbHelper.C_ID +"=1" //where clause
                , new String[]{String.valueOf(tripId)} //selectionArgs
                , null //groupBy
                , null //having
                , null //order by
                , null //limit
        );

        if(cursor.moveToFirst())
        {
            return new TripRow( cursor.getInt(0)
                    , cursor.getString(1)
                    , cursor.getDouble(2)
                    , cursor.getInt(3)
                    , cursor.getInt(4)
                    , cursor.getInt(5)
                    , cursor.getDouble(6)
                    , cursor.getString(7)
            );
        }
        return null;
    }

    public ArrayList<DataRow> selectTripData(int tripId)
    {
        ArrayList<DataRow> rows = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

//      declare cursor to read data
//      selectCarData row with highest id
        Cursor cursor = db.query( TABLE_CAR_DATA
                , null //columns
                , C_TRIP_ID + "=?" //DbHelper.C_ID +"=1" //where clause
                , new String[]{String.valueOf(tripId)} //selectionArgs
                , null //groupBy
                , null //having
                , null //order by
                , null //limit
        );

        for (int i = 0; i < cursor.getCount(); i++)
        {
            if(cursor.moveToPosition(i))
            {
                rows.add(new DataRow( cursor.getInt(0)
                        , cursor.getString(1)
                        , cursor.getDouble(2)
                        , cursor.getInt(3)
                        , cursor.getInt(4)
                        , cursor.getDouble(5)
                        , cursor.getInt(6)
                        , cursor.getInt(7)
                        , cursor.getDouble(8)
                        , cursor.getDouble(9)
                        , cursor.getDouble(10)
                        , cursor.getDouble(11)
                ));
            }
        }

        return rows;
    }

    public boolean deleteCarData(int id, Context c, String dateTime)
    {
        copyDatabase(c, dateTime);

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_CAR_DATA, C_TRIP_ID + "=?", new String[] {String.valueOf(id)}) > 0;
    }

    public static void copyDatabase(Context c, String dateTime)
    {
        try
        {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite())
            {
                String currentDBPath = "/data/data/" + c.getPackageName() + "/databases/" + DATABASE_NAME;
                String backupDBPath = "backup(" + dateTime + ").db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists())
                {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        }
        catch (Exception e)
        {
            Log.e("fuck off", e.toString());
        }
    }

    public boolean deleteTrip(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_TRIP, T_ID + "=?", new String[] {String.valueOf(id)}) > 0;
    }

    public void updateTripName(int id, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(T_NAME, name);

        db.update(TABLE_TRIP, values, T_ID + "=?", new String[] {String.valueOf(id)});
    }
}
