package obd2.dhbw.de.obd2_reader.util;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    public static boolean calculate( DbHelper dbHelper
                                , int tripId
                                , int readIntervall
                                , String tripName
                                , Context c
                                )
    {
        ArrayList<DataRow> rows = dbHelper.selectTripData(tripId);

        if (rows == null || rows.size() == 0) return false;

        for(DataRow row : rows)
        {
            int speed = row.getSpeed();

            //car does not move
            if(speed == 0){
                standTime += readIntervall/1000;
            }
            //just compute distance, if Lat/Long is set ( > 0), it may happen that in the first
            // iterations the location is null, so it it saved "0"
            if (row.getLongitude()>0 && row.getLatitude()>0) {
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
            runTime = Math.max(row.getRunTime(), runTime);
        }

        avgSpeed /= rows.size();

        distance = Math.round(distance);

        Log.d(LOG_TAG,  "runtime:  "  + runTime + "\n" +
                        "standTime: " + standTime + "\n" +
                        "maxSpeed: "  + maxSpeed + "\n" +
                        "avgSpeed: "  + avgSpeed + "\n" +
                        "distance: "  + distance);

        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();

        dbHelper.deleteCarData(tripId, c, dateTimeFormat.format(new Date()));

        return dbHelper.insertTripData( tripId
                                      , dateFormat.format(new Date())
                                      , distance
                                      , runTime
                                      , standTime
                                      , maxSpeed
                                      , avgSpeed
                                      , tripName
                                      );
    }
}
