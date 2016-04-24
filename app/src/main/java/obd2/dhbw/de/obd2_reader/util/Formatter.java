package obd2.dhbw.de.obd2_reader.util;

/**
 * Created by Ricardo on 20.04.2016.
 */
public class Formatter
{
    public static int formatInt(String value)
    {
        int formatted = 0;
        try
        {
            formatted = Integer.parseInt(value);
        }
        catch(Exception e){}

        return formatted;
    }

    public static double formatDouble(String value)
    {
        double formatted = 0;
        try
        {
            formatted = Double.parseDouble(value);
            formatted *= 100;
            formatted = (Math.round(formatted)) / (double) 100;
        }
        catch(Exception e){}

        return formatted;
    }
}

