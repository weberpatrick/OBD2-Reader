package obd2.dhbw.de.obd2_reader.container;

/**
 * Created by Ricardo on 28.04.2016.
 */
public class TripRow
{
    private int tripId;
    private int maxSpeed;
    private double avgSpeed;
    private int runTime;
    private int standTime;
    private double distance;
    private String date;
    private String name;

    public TripRow( int tripId
                  , String date
                  , double distance
                  , int runTime
                  , int standTime
                  , int maxSpeed
                  , double avgSpeed
                  )
    {
        this.maxSpeed = maxSpeed;
        this.tripId = tripId;
        this.date = date;
        this.avgSpeed = avgSpeed;
        this.runTime = runTime;
        this.standTime = standTime;
        this.distance = distance;
    }

    public TripRow( int tripId
                  , String date
                  , double distance
                  , int runTime
                  , int standTime
                  , int maxSpeed
                  , double avgSpeed
                  , String name
                  )
    {
        this.maxSpeed = maxSpeed;
        this.tripId = tripId;
        this.date = date;
        this.avgSpeed = avgSpeed;
        this.runTime = runTime;
        this.standTime = standTime;
        this.distance = distance;
        this.name = name;
    }

    public int getTripId()
    {
        return tripId;
    }

    public int getMaxSpeed()
    {
        return maxSpeed;
    }

    public double getAvgSpeed()
    {
        return avgSpeed;
    }

    public int getRunTime()
    {
        return runTime;
    }

    public int getStandTime()
    {
        return standTime;
    }

    public double getDistance()
    {
        return distance;
    }

    public String getDate()
    {
        return date;
    }

    public String getName()
    {
        return name;
    }
}
