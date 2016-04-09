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
//    http://www.sqldocu.com/four/createtab.htm

//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private static final String LOG_TAG = DbHelper.class.getName();

//  database
    private static final int DATABASE_VERSION   = 1;
    private static final String DATABASE_NAME   = "obdDB.db";
    public static final String TABLE_NAME       = "carData";

//  columns
    public static final String COLUMN_ID   = "id";
    public static final String COLUMN_NAME = "name";

//  sql commands
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME
                    + " ("
                    + COLUMN_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT NOT NULL"
                    + ");";

    private static final String TABLE_DROP = "DROP TABLE " + TABLE_NAME;

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
