package obd2.dhbw.de.obd2_reader.util;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import obd2.dhbw.de.obd2_reader.R;

/**
 * Created by Patrick on 12.04.2016.
 */
public class LocationFinder
       extends Service
       implements LocationListener
{

    private final String LOG_TAG = LocationFinder.class.getName();

    //minimum Distance between updates
    //is 0, because of high battery consumption.
    private final long UPDATE_DISTANCE = 0;
    //Minimum Time between updates
    private final long UPDATE_TIME = 1000 * 1;

    private Context context;
    private LocationManager locationManager;

    private boolean isGpsActive = false;
    private boolean isNetworkActive = false;

    private boolean canGetLocation = false;

    private Location lastLocation;

    /**
     *  Constructor
     */
    public LocationFinder(Context con) {
        this.context = con;
        startGps();
    }

    public void startGps() {

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        //get GPS/Network Status
        isGpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkActive = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGpsActive || isNetworkActive) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                canGetLocation = true;

                if (isGpsActive){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIME, UPDATE_DISTANCE, this);

//                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (isNetworkActive){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, UPDATE_DISTANCE, this);

//                    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                    if (isGood(loc)){
//                        lastLocation = loc;
//                    }
                }
            } else {
                Log.d(LOG_TAG, "No Location Permission");
                canGetLocation = false;
            }

        }else{
            canGetLocation = false;
        }
    }

    public void stopGps()
    {
        canGetLocation = false;
        if ( locationManager != null
          && ActivityCompat.checkSelfPermission( context
                , Manifest.permission.ACCESS_FINE_LOCATION)
           == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.removeUpdates(this);
        }
    }

    /**
     *  http://developer.android.com/guide/topics/location/strategies.html
     */
    private boolean isGood(Location newLocation) {
        if (lastLocation == null) {
            // A new location is always better than no location
            return true;
        }
        if (newLocation == null){
            return false;
        }
        //return true if Accuracy is less than 100
        return (newLocation.getAccuracy() < 100);
    }

    public void showGPSAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle(R.string.gpsSettingTitle);
        alertDialog.setMessage(R.string.gpsSettingsMessage);

        // Pressing Settings Button
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // Pressing Cancel Button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //if User cancels the dialog
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(context, R.string.gpsToastMessage, Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location newLocation)
    {
        if (isGood(newLocation)) lastLocation = newLocation;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {
        startGps();
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            isGpsActive = false;
        }
        if (provider.equals(LocationManager.NETWORK_PROVIDER)){
            isNetworkActive = false;
        }
        if (!isGpsActive && !isNetworkActive)
        {
            showGPSAlert();
            stopGps();
        }
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    public double getLatitude()
    {
        if (lastLocation != null) return lastLocation.getLatitude();
        else return 0;
    }

    public double getLongitude()
    {
        if (lastLocation != null) return lastLocation.getLongitude();
        else return 0;
    }

    public double getAltitude()
    {
        if (lastLocation != null) return lastLocation.getAltitude();
        else return 0;
    }

    public double getSpeed() {
        if (lastLocation != null && lastLocation.hasSpeed())
        {
            // speed in m/s to km/h
            return lastLocation.getSpeed()*3.6;
        }
        else return -1;
    }
}
