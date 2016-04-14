package obd2.dhbw.de.obd2_reader.container;

/**
 * Created by Ricardo on 12.04.2016.
 */
public class DataRow
{
    private int id;
    private String timestamp;
    private double engineLoad;
    private double intakeManifoldPressure;
    private int rpm;
    private int speed;
    private double timingAdvance;
    private double throttlePosition;
    private int runTime;
    private double barometricPressure;
    private double widebandAirFuelRatio;
    private double absoluteLoad;
    private double airFuelRatio;
    private int tripId;
    private double gpsSpeed;
    private double latitude;
    private double longitude;
    private double altitude;

    public DataRow( int id
                  , String timestamp
                  , double engineLoad
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
        this.id = id;
        this.timestamp = timestamp;
        this.engineLoad = engineLoad;
        this.intakeManifoldPressure = intakeManifoldPressure;
        this.rpm = rpm;
        this.speed = speed;
        this.timingAdvance = timingAdvance;
        this.throttlePosition = throttlePosition;
        this.runTime = runTime;
        this.barometricPressure = barometricPressure;
        this.widebandAirFuelRatio = widebandAirFuelRatio;
        this.absoluteLoad = absoluteLoad;
        this.airFuelRatio = airFuelRatio;
        this.tripId = tripId;
        this.gpsSpeed = gpsSpeed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

//    TODO add the units inside the getter methods ?!

//    TODO check whether we need the id or not
//    public int getId()
//    {
//        return id;
//    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public String getEngineLoad()
    {
        return engineLoad + " %";
    }

    public String getIntakeManifoldPressure()
    {
        return intakeManifoldPressure + " kPa";
    }

    public String getRpm()
    {
        return String.valueOf(rpm);
    }

    public String getSpeed()
    {
        return speed + " km/h";
    }

    public String getTimingAdvance()
    {
        return timingAdvance + " %";
    }

    public String getThrottlePosition()
    {
        return throttlePosition + " %";
    }

    public String getRunTime()
    {
        return runTime / 60 + " min " + runTime % 60 + " sek";
    }

    public String getBarometricPressure()
    {
        return barometricPressure + " kPa";
    }

    public String getWidebandAirFuelRatio()
    {
        return widebandAirFuelRatio + " LBV";
    }

    public String getAbsoluteLoad()
    {
        return absoluteLoad + " %";
    }

    public String getAirFuelRatio()
    {
        return airFuelRatio + " LBV";
    }

//    TODO uncomment when we need it
//    public String getTripId()
//    {
//        return tripId;
//    }

    public String getGpsSpeed()
    {
        return gpsSpeed + " km/h";
    }

    public String getLatitude()
    {
        return latitude + " Grad";
    }

    public String getLongitude()
    {
        return longitude + " Grad";
    }

    public String getAltitude()
    {
        return altitude + " m";
    }
}
