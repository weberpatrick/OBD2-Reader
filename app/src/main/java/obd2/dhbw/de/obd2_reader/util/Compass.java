package obd2.dhbw.de.obd2_reader.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Patrick on 18.04.2016.
 */
public class Compass
{
    private final String LOG_TAG = Compass.class.getName();

    private SensorManager sensorManager;

    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];

    private float[] r = new float[9];
    private float[] orientation = new float[3];

    private boolean isAccelerometerSet = false;
    private boolean isMagnetometerSet = false;

    private Context context;

    private float azimuth = 0;
    private float lastAzimuth = 0;

    public Compass(Context context) {
        this.context = context;
        start();
    }

    private void start() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        registerListener();
    }

    private SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == accelerometer) {
                lastAccelerometer = event.values.clone();
                isAccelerometerSet = true;
            } else if (event.sensor == magnetometer) {
                lastMagnetometer = event.values.clone();
                isMagnetometerSet = true;
            }

            if (isAccelerometerSet && isMagnetometerSet) {
                SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer);
                SensorManager.getOrientation(r, orientation);

                float azimuthInRadians = orientation[0];

                azimuth = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public void unregisterListener()
    {
        sensorManager.unregisterListener(orientationListener);
    }

    public void registerListener()
    {
        sensorManager.registerListener(orientationListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(orientationListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public float getRotation() {
        lastAzimuth = azimuth;
        return azimuth;
    }

    public float getLastRotation() {
        return lastAzimuth;
    }
}
