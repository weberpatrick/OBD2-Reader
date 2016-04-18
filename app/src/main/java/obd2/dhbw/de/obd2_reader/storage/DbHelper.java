package obd2.dhbw.de.obd2_reader.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import obd2.dhbw.de.obd2_reader.container.DataRow;

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
    private static final String DATABASE_NAME   = "obdDB.db";
    public static final String TABLE_CAR_DATA   = "carData";
    public static final String TABLE_TRIP       = "trip";

//	***************************************************************************
//	columns
//	***************************************************************************

//  car data columns
    private static final String C_ID                         = TABLE_CAR_DATA + "_" + "id";
    private static final String C_TIMESTAMP                  = TABLE_CAR_DATA + "_" + "timestamp";
    private static final String C_ENGINE_LOAD                = TABLE_CAR_DATA + "_" + "engineLoad";
    private static final String C_INTAKE_MANIFOLD_PRESSURE   = TABLE_CAR_DATA + "_" + "intakeManifoldPressure";
    private static final String C_RPM                        = TABLE_CAR_DATA + "_" + "rpm";
    private static final String C_SPEED                      = TABLE_CAR_DATA + "_" + "speed";
    private static final String C_TIMING_ADVANCE             = TABLE_CAR_DATA + "_" + "timingAdvance";
    private static final String C_THROTTLE_POSITION          = TABLE_CAR_DATA + "_" + "throttlePosition";
    private static final String C_RUNTIME                    = TABLE_CAR_DATA + "_" + "runTime";
    private static final String C_BAROMETRIC_PRESSURE        = TABLE_CAR_DATA + "_" + "barometricPressure";
    private static final String C_WIDEBAND_AIR_FUEL_RATIO    = TABLE_CAR_DATA + "_" + "widebandAirFuelRatio";
    private static final String C_ABSOLUTE_LOAD              = TABLE_CAR_DATA + "_" + "absoluteLoad";
    private static final String C_AIR_FUEL_RATIO             = TABLE_CAR_DATA + "_" + "airFuelRatio";

//  TODO check whether a foreign key connection is necessary
    private static final String C_TRIP_ID                    = TABLE_CAR_DATA + "_" + "tripId";
    private static final String C_GPS_SPEED                  = TABLE_CAR_DATA + "_" + "gpsSpeed";
    private static final String C_LATITUDE                   = TABLE_CAR_DATA + "_" + "latitude";
    private static final String C_LONGITUDE                  = TABLE_CAR_DATA + "_" + "longitude";
    private static final String C_ALTITUDE                   = TABLE_CAR_DATA + "_" + "altitude";

//  trip columns
    private static final String T_ID                        = TABLE_TRIP + "_" + "id";
    private static final String T_TRACK_LENGTH              = TABLE_TRIP + "_" + "trackLength";
    private static final String T_DRIVING_TIME              = TABLE_TRIP + "_" + "drivingTime";
    private static final String T_STAND_TIME                = TABLE_TRIP + "_" + "standTime";
    private static final String T_MAX_SPEED                 = TABLE_TRIP + "_" + "maxSpeed";
    private static final String T_AVG_SPEED                 = TABLE_TRIP + "_" + "avgSpeed";

//  sql commands
    private static final String TABLE_CAR_DATA_CREATE =
            "CREATE TABLE " + TABLE_CAR_DATA
                    + " ("
                    + C_ID                      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + C_TIMESTAMP               + " TEXT DEFAULT CURRENT_TIMESTAMP,"
                    + C_ENGINE_LOAD             + " REAL,"
                    + C_INTAKE_MANIFOLD_PRESSURE+ " REAL,"
                    + C_RPM                     + " INTEGER,"
                    + C_SPEED                   + " INTEGER,"
                    + C_TIMING_ADVANCE          + " REAL,"
                    + C_THROTTLE_POSITION       + " REAL,"
                    + C_RUNTIME                 + " INTEGER,"
                    + C_BAROMETRIC_PRESSURE     + " REAL,"
                    + C_WIDEBAND_AIR_FUEL_RATIO + " REAL,"
                    + C_ABSOLUTE_LOAD           + " REAL,"
                    + C_AIR_FUEL_RATIO          + " REAL,"
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
                    + T_TRACK_LENGTH            + " REAL,"
                    + T_DRIVING_TIME            + " INTEGER,"
                    + T_STAND_TIME              + " INTEGER,"
                    + T_MAX_SPEED               + " INTEGER,"
                    + T_AVG_SPEED               + " REAL"
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
                                , double intakeManifoldPressure
                                , int rpm
                                , int speed
                                , double timingAdvance
                                , double throttlePosition
                                , int runTime
                                , double barometricPressure
                                , double widebandAirFuelRatio
                                , double absoluteLoad
                                , double airFuelRatio
                                , int tripId
                                , double gpsSpeed
                                , double latitude
                                , double longitude
                                , double altitude
                                )
    {
        ContentValues values = new ContentValues();
        values.put(C_ENGINE_LOAD, engineLoad);
        values.put(C_INTAKE_MANIFOLD_PRESSURE, intakeManifoldPressure);
        values.put(C_RPM, rpm);
        values.put(C_SPEED, speed);
        values.put(C_TIMING_ADVANCE, timingAdvance);
        values.put(C_THROTTLE_POSITION, throttlePosition);
        values.put(C_RUNTIME, runTime);
        values.put(C_BAROMETRIC_PRESSURE, barometricPressure);
        values.put(C_WIDEBAND_AIR_FUEL_RATIO, widebandAirFuelRatio);
        values.put(C_ABSOLUTE_LOAD, absoluteLoad);
        values.put(C_AIR_FUEL_RATIO, airFuelRatio);
        values.put(C_TRIP_ID, tripId);
        values.put(C_GPS_SPEED, gpsSpeed);
        values.put(C_LATITUDE, latitude);
        values.put(C_LONGITUDE, longitude);
        values.put(C_ALTITUDE, altitude);

        SQLiteDatabase db = this.getWritableDatabase();
        long insertId = db.insert(TABLE_CAR_DATA, null, values);

        Log.d(LOG_TAG, "insertCarData ID: " + insertId);

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

        Log.d(LOG_TAG, "cursor length: " + cursor.getCount());

        if(cursor.moveToPosition(0))
        {
            return new DataRow( cursor.getInt(0)
                              , cursor.getString(1)
                              , cursor.getDouble(2)
                              , cursor.getDouble(3)
                              , cursor.getInt(4)
                              , cursor.getInt(5)
                              , cursor.getDouble(6)
                              , cursor.getDouble(7)
                              , cursor.getInt(8)
                              , cursor.getDouble(9)
                              , cursor.getDouble(10)
                              , cursor.getDouble(11)
                              , cursor.getDouble(12)
                              , cursor.getInt(13)
                              , cursor.getDouble(14)
                              , cursor.getDouble(15)
                              , cursor.getDouble(16)
                              , cursor.getDouble(17)
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
                , null //C_ID +"=1" //where clause
                , null //selectionArgs
                , null //groupBy
                , null //having
                , T_ID + " DESC" //order by
                , "1" //limit
        );

        Log.d(LOG_TAG, "cursor length: " + cursor.getCount());

        if(cursor.moveToPosition(0)) return cursor.getInt(0);

        return 0;
    }

    public boolean insertTripData( int tripId
                                 , double trackLength
                                 , int drivingTime
                                 , double standTime
                                 , double maxSpeed
                                 , double avgSpeed
                                 )
    {
        ContentValues values = new ContentValues();
        values.put(T_ID, tripId);
        values.put(T_TRACK_LENGTH, trackLength);
        values.put(T_DRIVING_TIME, drivingTime);
        values.put(T_STAND_TIME, standTime);
        values.put(T_MAX_SPEED, maxSpeed);
        values.put(T_AVG_SPEED, avgSpeed);

        SQLiteDatabase db = this.getWritableDatabase();
        long insertId = db.insert(TABLE_TRIP, null, values);

        Log.d(LOG_TAG, "insertTripId: " + insertId);

//      insertID = -1 means the insertCarData failed
        if(insertId == -1) return false;

        return true;
    }

    public ArrayList<DataRow> selectTripData(int tripId)
    {
        ArrayList<DataRow> rows = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

//      declare cursor to read data
//      selectCarData row with highest id
        Cursor cursor = db.query( TABLE_TRIP
                , null //columns
                , T_ID + "=" + tripId //DbHelper.C_ID +"=1" //where clause
                , null //selectionArgs
                , null //groupBy
                , null //having
                , null //order by
                , null //limit
        );

        Log.d(LOG_TAG, "cursor length: " + cursor.getCount());

        for (int i = 0; i < cursor.getCount(); i++)
        {
            if(cursor.moveToPosition(i))
            {
                rows.add(new DataRow( cursor.getInt(0)
                        , cursor.getString(1)
                        , cursor.getDouble(2)
                        , cursor.getDouble(3)
                        , cursor.getInt(4)
                        , cursor.getInt(5)
                        , cursor.getDouble(6)
                        , cursor.getDouble(7)
                        , cursor.getInt(8)
                        , cursor.getDouble(9)
                        , cursor.getDouble(10)
                        , cursor.getDouble(11)
                        , cursor.getDouble(12)
                        , cursor.getInt(13)
                        , cursor.getDouble(14)
                        , cursor.getDouble(15)
                        , cursor.getDouble(16)
                        , cursor.getDouble(17)
                ));
            }
        }

        return rows;
    }
}
