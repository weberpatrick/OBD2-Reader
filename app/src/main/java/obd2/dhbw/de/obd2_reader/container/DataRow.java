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

    public double getIntakeManifoldPressure() {
        return intakeManifoldPressure;
    }

    public int getRpm() {
        return rpm;
    }

    public int getSpeed() {
        return speed;
    }

    public double getTimingAdvance() {
        return timingAdvance;
    }

    public double getThrottlePosition() {
        return throttlePosition;
    }

    public int getRunTime() {
        return runTime;
    }

    public double getBarometricPressure() {
        return barometricPressure;
    }

    public double getWidebandAirFuelRatio() {
        return widebandAirFuelRatio;
    }

    public double getAbsoluteLoad() {
        return absoluteLoad;
    }

    public double getAirFuelRatio() {
        return airFuelRatio;
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

    public String getIntakeManifoldPressureString()
    {
        return intakeManifoldPressure + " kPa";
    }

    public String getRpmString()
    {
        return String.valueOf(rpm);
    }

    public String getSpeedString()
    {
        return speed + " km/h";
    }

    public String getTimingAdvanceString()
    {
        return timingAdvance + " %";
    }

    public String getThrottlePositionString()
    {
        return throttlePosition + " %";
    }

    public String getRunTimeString()
    {
        return runTime / 60 + " min " + runTime % 60 + " sek";
    }

    public String getBarometricPressureString()
    {
        return barometricPressure + " kPa";
    }

    public String getWidebandAirFuelRatioString()
    {
        return widebandAirFuelRatio + " LBV";
    }

    public String getAbsoluteLoadString()
    {
        return absoluteLoad + " %";
    }

    public String getAirFuelRatioString()
    {
        return airFuelRatio + " LBV";
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
