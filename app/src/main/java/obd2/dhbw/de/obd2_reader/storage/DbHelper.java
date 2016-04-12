package obd2.dhbw.de.obd2_reader.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    public static final String TABLE_CAR_DATA = "carData";

//  columns
    public static final String C_ID                         = "id";
    public static final String C_TIMESTAMP                  = "timestamp";
    public static final String C_ENGINE_LOAD                = "engineLoad";
    public static final String C_INTAKE_MANIFOLD_PRESSURE   = "intakeManifoldPressure";
    public static final String C_RPM                        = "rpm";
    public static final String C_SPEED                      = "speed";
    public static final String C_TIMING_ADVANCE             = "timingAdvance";
    public static final String C_THROTTLE_POSITION          = "throttlePosition";
    public static final String C_RUNTIME                    = "runTime";
    public static final String C_BAROMETRIC_PRESSURE        = "barometricPressure";
    public static final String C_WIDEBAND_AIR_FUEL_RATIO    = "widebandAirFuelRatio";
    public static final String C_ABSOLUTE_LOAD              = "absoluteLoad";
    public static final String C_AIR_FUEL_RATIO             = "airFuelRatio";

//  TODO check whether a foreign key connection is necessary
    public static final String C_TRIP_ID                    = "tripId";
    public static final String C_GPS_SPEED                  = "gpsSpeed";
    public static final String C_LATITUDE                   = "latitude";
    public static final String C_LONGITUDE                  = "longitude";
    public static final String C_ALTITUDE                   = "altitude";

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
                    + C_AIR_FUEL_RATIO          + " REAL"
                    + C_TRIP_ID                 + " INTEGER,"
                    + C_GPS_SPEED               + " REAL,"
                    + C_LATITUDE                + " REAL,"
                    + C_LONGITUDE               + " REAL,"
                    + C_ALTITUDE                + " REAL"
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
            Log.d(LOG_TAG, "Create the table with the SQL command: " + TABLE_CAR_DATA_CREATE);

            db.execSQL(TABLE_CAR_DATA_CREATE);
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

    public boolean insert( double engineLoad
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
//        TODO integrate gps speed and position stuff
        values.put(C_GPS_SPEED, gpsSpeed);
        values.put(C_LATITUDE, latitude);
        values.put(C_LONGITUDE, longitude);
        values.put(C_ALTITUDE, altitude);

        SQLiteDatabase db = this.getWritableDatabase();
        long insertId = db.insert(TABLE_CAR_DATA, null, values);

        Log.d(LOG_TAG, "insert ID: " + insertId);

//      insertID = -1 means the insert failed
        if(insertId == -1) return false;

        return true;
    }

    public DataRow select()
    {
        SQLiteDatabase db = this.getReadableDatabase();

//      declare cursor to read data
//      select row with highest id
        Cursor cursor = db.query( TABLE_CAR_DATA
                                , null //columns
                                , null //DbHelper.C_ID +"=1" //where clause
                                , null //selectionArgs
                                , null //groupBy
                                , null //having
                                , C_ID + " DESC" //order by
                                , "1" //limit
        );

        Log.d(LOG_TAG, "cursor length: " + cursor.getCount());

        if(cursor.moveToPosition(0))
        {
            Log.d(LOG_TAG, String.valueOf(cursor.getInt(0)));
            Log.d(LOG_TAG, cursor.getString(1));

            String.valueOf(cursor.getDouble(2));

            String.valueOf(cursor.getDouble(3));
            String.valueOf(cursor.getDouble(4));

            String.valueOf(cursor.getDouble(5));
            String.valueOf(cursor.getDouble(6));
            String.valueOf(cursor.getDouble(7));
            String.valueOf(cursor.getInt(8));
            String.valueOf(cursor.getDouble(9));
            String.valueOf(cursor.getDouble(10));
            String.valueOf(cursor.getDouble(11));
            String.valueOf(cursor.getDouble(12));
        }

//        TODO create a data row object with values from cursor
//        return new DataRow();
        return null;
    }
}
