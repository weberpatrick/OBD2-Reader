package obd2.dhbw.de.obd2_reader.util;

import android.util.Log;

import java.util.ArrayList;

import obd2.dhbw.de.obd2_reader.container.DataRow;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;

/**
 * Created by Ricardo on 16.04.2016.
 */
public class TripCalculator
{
    private static final String LOG_TAG = TripCalculator.class.getName();

    private static int maxSpeed = 0;
    private static int avgSpeed = 0;
    private static int runTime = 0;

    public static boolean calculate(DbHelper dbHelper, int tripId)
    {
        Log.d(LOG_TAG, "calculate trip stuff");

        ArrayList<DataRow> rows = dbHelper.selectTripData(tripId);

        if (rows == null || rows.size() == 0) return false;

        for(DataRow row : rows)
        {
//            TODO calculate trip length
//            TODO calculate stand time
            int speed = row.getSpeed();
            maxSpeed = Math.max(speed, maxSpeed);
            avgSpeed += speed;
            runTime = Math.max(row.getRunTime(), runTime);
        }

        avgSpeed /= rows.size();

        Log.d(LOG_TAG, runTime + " : " + maxSpeed + " : " + avgSpeed);

        return dbHelper.insertTripData( tripId, 0.0, runTime, 0, maxSpeed, avgSpeed);
    }
}
