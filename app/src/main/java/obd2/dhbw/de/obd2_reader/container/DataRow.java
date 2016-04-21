package obd2.dhbw.de.obd2_reader.container;

/**
 * Created by Ricardo on 12.04.2016.
 */
public class DataRow
{
    private int id;
    private String timestamp;
    private double engineLoad;
    private int rpm;
    private int speed;
    private double throttlePosition;
    private int runTime;
    private int tripId;
    private double gpsSpeed;
    private double latitude;
    private double longitude;
    private double altitude;

    public DataRow( int id
                  , String timestamp
                  , double engineLoad
                  , int rpm
                  , int speed
                  , double throttlePosition
                  , int runTime
                  , int tripId
                  , double gpsSpeed
                  , double latitude
                  , double longitude
                  , double altitude
                  )
    {
        this.id = id;
        this.timestamp = timestamp;
        this.engineLoad = engineLoad;
        this.rpm = rpm;
        this.speed = speed;
        this.throttlePosition = throttlePosition;
        this.runTime = runTime;
        this.tripId = tripId;
        this.gpsSpeed = gpsSpeed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

//    TODO check whether we need the id or not
//    public int getId()
//    {
//        return id;
//    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public String getEngineLoadString()
    {
        return engineLoad + " %";
    }

    public int getId() {
        return id;
    }

    public double getEngineLoad() {
        return engineLoad;
    }

    public int getRpm() {
        return rpm;
    }

    public int getSpeed() {
        return speed;
    }

    public double getThrottlePosition() {
        return throttlePosition;
    }

    public int getRunTime() {
        return runTime;
    }

    public int getTripId() {
        return tripId;
    }

    public double getGpsSpeed() {
        return gpsSpeed;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public String getRpmString()
    {
        return String.valueOf(rpm);
    }

    public String getSpeedString()
    {
        return speed + " km/h";
    }

    public String getThrottlePositionString()
    {
        return throttlePosition + " %";
    }

    public String getRunTimeString()
    {
        return runTime / 60 + " min " + runTime % 60 + " sek";
    }

//    TODO uncomment when we need it
//    public String getTripId()
//    {
//        return tripId;
//    }

    public String getGpsSpeedString()
    {
        return gpsSpeed + " km/h";
    }

    public String getLatitudeString()
    {
        return latitude + " Grad";
    }

    public String getLongitudeString()
    {
        return longitude + " Grad";
    }

    public String getAltitudeString()
    {
        return altitude + " m";
    }
}
