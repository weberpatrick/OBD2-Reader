package obd2.dhbw.de.obd2_reader;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Punika on 02.04.2016.
 */
public class DbHelper
       extends SQLiteOpenHelper
{
//    http://www.programmierenlernenhq.de/daten-in-sqlite-datenbank-schreiben-und-lesen-in-android/

    private static final String LOG_TAG = DbHelper.class.getName();

//  database
    private static final int DATABASE_VERSION        = 1;
    private static final String DATABASE_NAME        = "testDB.db";
    public static final String DICTIONARY_TABLE_NAME = "testTable";

//  columns
    public static final String COLUMN_ID   = "_id";
    public static final String COLUMN_NAME = "name";

//  sql commands
    private static final String DICTIONARY_TABLE_CREATE =
        "CREATE TABLE " + DICTIONARY_TABLE_NAME
        + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
               + COLUMN_NAME + " TEXT NOT NULL);";

    private static final String DICTIONARY_TABLE_DROP =
            "DROP TABLE " + DICTIONARY_TABLE_NAME;

    DbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void dropTable(SQLiteDatabase db)
    {
        try
        {
            db.execSQL(DICTIONARY_TABLE_DROP);
            Log.d(LOG_TAG, "Drop table with the SQL command: " + DICTIONARY_TABLE_DROP);
        }
        catch (SQLException e)
        {
            Log.d(LOG_TAG, "Drop table failed");
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            Log.d(LOG_TAG, "Create the table with the SQL command: " + DICTIONARY_TABLE_CREATE);

            db.execSQL(DICTIONARY_TABLE_CREATE);
        }
        catch (SQLException e)
        {
            Log.e(LOG_TAG, "Error while creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
