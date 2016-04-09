package obd2.dhbw.de.obd2_reader.storage;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

//  sql commands
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_CAR_DATA
                    + " ("
                    + C_ID                      + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + C_TIMESTAMP               + " TEXT DEFAULT CURRENT_TIMESTAMP,"
                    + C_ENGINE_LOAD             + " REAL,"
                    + C_INTAKE_MANIFOLD_PRESSURE+ " REAL,"
                    + C_RPM                     + " REAL,"
                    + C_SPEED                   + " REAL,"
                    + C_TIMING_ADVANCE          + " REAL,"
                    + C_THROTTLE_POSITION       + " REAL,"
                    + C_RUNTIME                 + " INTEGER,"
                    + C_BAROMETRIC_PRESSURE     + " REAL,"
                    + C_WIDEBAND_AIR_FUEL_RATIO + " REAL,"
                    + C_ABSOLUTE_LOAD           + " REAL,"
                    + C_AIR_FUEL_RATIO          + " REAL"
                    + ");";

    private static final String TABLE_DROP = "DROP TABLE " + TABLE_CAR_DATA;

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
            Log.d(LOG_TAG, "Create the table with the SQL command: " + TABLE_CREATE);

            db.execSQL(TABLE_CREATE);
        }
        catch (SQLException e)
        {
            Log.e(LOG_TAG, "Error while creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


    public void dropTable(SQLiteDatabase db)
    {
        try
        {
            db.execSQL(TABLE_DROP);
            Log.d(LOG_TAG, "Drop table with the SQL command: " + TABLE_DROP);
        }
        catch (SQLException e)
        {
            Log.d(LOG_TAG, "Drop table failed");
            e.printStackTrace();
        }
    }
}
