package obd2.dhbw.de.obd2_reader.util;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.TimingAdvanceCommand;
import com.github.pires.obd.commands.engine.AbsoluteLoadCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_01_20;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_41_60;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;

import obd2.dhbw.de.obd2_reader.storage.DbHelper;

/**
 * Created by Ricar on 08.04.2016.
 */
public class InputDataReader
{
//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = InputDataReader.class.getName();

    private enum RESULT_FORMAT
    {
        NO_RESULT
        , RAW
        , CALCULATED
        , FORMATTED
    }

//	***************************************************************************
//	DECLARATION OF VARIABLES
//	***************************************************************************

    private BluetoothSocket socket;
    private DbHelper dbHelper;

    private ArrayList<ObdCommand> availableCommands;

//	***************************************************************************
//	CONSTRUCTOR AREA
//	***************************************************************************

    public InputDataReader(DbHelper dbHelper, BluetoothSocket socket)
    {
        this.socket = socket;
        this.dbHelper = dbHelper;

        initOdb();

        determineAvailablePids();
    }

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    /**
     * Initialize the obd adapter.
     */
    private void initOdb()
    {
        Log.d(LOG_TAG, "initialize obd");
        try
        {
            executeCommand(new ObdResetCommand()   , RESULT_FORMAT.NO_RESULT);
            executeCommand(new EchoOffCommand()    , RESULT_FORMAT.NO_RESULT);
            executeCommand(new LineFeedOffCommand(), RESULT_FORMAT.NO_RESULT);
            executeCommand(new TimeoutCommand(125) , RESULT_FORMAT.NO_RESULT);
            executeCommand(new SelectProtocolCommand(ObdProtocols.AUTO), RESULT_FORMAT.NO_RESULT);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "exception in initObd(): " + e.getMessage());
        }
    }

    /**
     * Executes the delivered command.
     *
     * @param command ObdCommand to be executed
     * @param format RESULT_FORMAT enumeration for the return format
     *
     * @return string result of the executed command
     */
    private String executeCommand(ObdCommand command, RESULT_FORMAT format)
    {
        try
        {
            if(socket.isConnected())
            {
                command.run(socket.getInputStream(), socket.getOutputStream());
                switch (format)
                {
                    case NO_RESULT:     return null;

                    case CALCULATED:    return command.getCalculatedResult();
                    case FORMATTED:     return command.getFormattedResult();
                    case RAW:           return command.getResult();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Determine all available pids and collect them in array list
     * 'availableCommands'.
     */
    private void determineAvailablePids()
    {
        availableCommands = new ArrayList<>();

        String hex_01_20 = executeCommand( new AvailablePidsCommand_01_20()
                                         , RESULT_FORMAT.FORMATTED);
        String hex_21_40 = executeCommand( new AvailablePidsCommand_21_40()
                                         , RESULT_FORMAT.FORMATTED);
        String hex_41_60 = executeCommand( new AvailablePidsCommand_41_60()
                                         , RESULT_FORMAT.FORMATTED);

        Log.d(LOG_TAG, hex_01_20);
        Log.d(LOG_TAG, hex_21_40);
        Log.d(LOG_TAG, hex_41_60);

        availableCommands.addAll(
                AvailableCommands.determineCommands(hex_01_20, AvailableCommands.PidArea.PIDS_01_20));

        availableCommands.addAll(
                AvailableCommands.determineCommands(hex_21_40, AvailableCommands.PidArea.PIDS_21_40));

        availableCommands.addAll(
                AvailableCommands.determineCommands(hex_41_60, AvailableCommands.PidArea.PIDS_41_60));
    }



    private int formatInt(String value)
    {
        int formatted = 0;
        try
        {
            formatted = Integer.parseInt(value);
        }
        catch(NumberFormatException nfe){}

        return formatted;
    }

    private double formatDouble(String value)
    {
        double formatted = 0;
        try
        {
            formatted = Double.parseDouble(value);
            formatted *= 100;
            formatted = (Math.round(formatted)) / (double)100;
        }
        catch(NumberFormatException nfe){}

        return formatted;
    }

    public void start()
    {
//        for(ObdCommand command : availableCommands)
//            Log.i(LOG_TAG, command.getName() + ": " + executeCommand(command, RESULT_FORMAT.CALCULATED));

        dbHelper.insert( formatDouble(executeCommand(new LoadCommand()              , RESULT_FORMAT.CALCULATED))
                   , formatDouble(executeCommand(new IntakeManifoldPressureCommand(), RESULT_FORMAT.CALCULATED))
                   , formatInt(executeCommand(new RPMCommand()                      , RESULT_FORMAT.CALCULATED))
                   , formatInt(executeCommand(new SpeedCommand()                    , RESULT_FORMAT.CALCULATED))
                   , formatDouble(executeCommand(new TimingAdvanceCommand()         , RESULT_FORMAT.CALCULATED))
                   , formatDouble(executeCommand(new ThrottlePositionCommand()      , RESULT_FORMAT.CALCULATED))
                   , formatInt(executeCommand(new RuntimeCommand()                  , RESULT_FORMAT.CALCULATED))
                   , formatDouble(executeCommand(new BarometricPressureCommand()    , RESULT_FORMAT.CALCULATED))
                   , formatDouble(executeCommand(new WidebandAirFuelRatioCommand()  , RESULT_FORMAT.CALCULATED))
                   , formatDouble(executeCommand(new AbsoluteLoadCommand()          , RESULT_FORMAT.CALCULATED))
                   , formatDouble(executeCommand(new AirFuelRatioCommand()          , RESULT_FORMAT.CALCULATED))
                   , 1 //TODO determine trip id
                   , 0 //TODO determine gps speed
                   , 0 //TODO determine latitude
                   , 0 //TODO determine longitude
                   , 0 //TODO determine altitude
                   );
    }
}
