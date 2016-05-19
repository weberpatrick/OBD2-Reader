package obd2.dhbw.de.obd2_reader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import obd2.dhbw.de.obd2_reader.R;
import obd2.dhbw.de.obd2_reader.container.DataRow;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;

/**
 * Created by Ricardo on 16.04.2016.
 */
public class TripCalculator
{
    private static final String LOG_TAG = TripCalculator.class.getName();

    private static int maxSpeed  = 0;
    private static int avgSpeed  = 0;
    private static int runTime   = 0;
    private static int standTime = 0;

    private static Location locOld = new Location("");
    private static Location locNew = new Location("");
    private static float distance = 0;
    private static boolean oldSet = false;

    private static Context context;

    public static boolean calculate( DbHelper dbHelper
                                , int tripId
                                , long standTime
                                , long drivingTime
                                , String tripName
                                , Context c
                                )
    {
        context = c;

        ArrayList<DataRow> rows = dbHelper.selectTripData(tripId);

        if (rows == null || rows.size() == 0) return false;

        for(DataRow row : rows)
        {
            int speed = row.getSpeed();
            //just compute distance, if Lat/Long is set ( > 0), it may happen that in the first
            // iterations the location is null, so it it saved "0"
            if (row.getLongitude()>0 && row.getLatitude()>0)
            {
                //calculate trip length
                locNew.setLatitude(row.getLatitude());
                locNew.setLongitude(row.getLongitude());

                //just compute distance if both locations are set AND speed > 0
                // in the first iteration: just locNew is set
                // if speed == 0 no distance is calculated, because the car is not moving,
                // but Lat/Long are changing minimal

                if (oldSet && (speed > 0))
                    distance += locOld.distanceTo(locNew);

                //the new location gets the old location. So the old is set
                locOld.setLatitude(locNew.getLatitude());
                locOld.setLongitude(locNew.getLongitude());

                oldSet = true;
            }
            maxSpeed = Math.max(speed, maxSpeed);
            avgSpeed += speed;
        }

        avgSpeed /= rows.size();

        distance = Math.round(distance);

        Log.d(LOG_TAG,  "engine runtime:  "  + drivingTime + "\n" +
                        "standTime: " + standTime + "\n" +
                        "maxSpeed: "  + maxSpeed + "\n" +
                        "avgSpeed: "  + avgSpeed + "\n" +
                        "distance: "  + distance);

        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();

        saveParkPosition();
        dbHelper.deleteCarData(tripId, context, dateTimeFormat.format(new Date()));

        return dbHelper.insertTripData( tripId
                                      , dateFormat.format(new Date())
                                      , distance
                                      , drivingTime
                                      , standTime
                                      , maxSpeed
                                      , avgSpeed
                                      , tripName
                                      );
    }

    public static void saveParkPosition() {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (locNew.getLatitude() != 0 && locNew.getLongitude() != 0)
        {
            Log.d("XXX", "saved park");
            //Save the double values as Long
            //http://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
            //http://stackoverflow.com/questions/3604849/where-to-save-android-gps-latitude-longitude-points
            editor.putLong(context.getString(R.string.pref_latitude), Double.doubleToLongBits(locNew.getLatitude()));
            editor.putLong(context.getString(R.string.pref_longitude), Double.doubleToLongBits(locNew.getLongitude()));

            editor.apply();

            //TODO Handler drum rum
            Toast.makeText(context, R.string.ParkPositionSaved, Toast.LENGTH_SHORT).show();
        }
        else
        {
            Log.d("XXX", "Failed to save");
            editor.remove(context.getString(R.string.pref_latitude));
            editor.remove(context.getString(R.string.pref_longitude));
            editor.apply();

            //TODO Handler drum rum
            Toast.makeText(context, R.string.ParkPositionNotSaved, Toast.LENGTH_SHORT).show();
        }
    }
}
