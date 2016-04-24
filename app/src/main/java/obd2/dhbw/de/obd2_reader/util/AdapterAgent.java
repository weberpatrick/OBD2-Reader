package obd2.dhbw.de.obd2_reader.util;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_01_20;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_41_60;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnableToConnectException;

import java.io.IOException;
import java.util.ArrayList;

import obd2.dhbw.de.obd2_reader.storage.DbHelper;

/**
 * Created by Ricardo on 08.04.2016.
 */
public class AdapterAgent
{
//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = AdapterAgent.class.getName();

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
    private ArrayList<Pair<String, String>> liveDataArray;

    private LocationFinder locationFinder;

//	***************************************************************************
//	CONSTRUCTOR AREA
//	***************************************************************************

    public AdapterAgent(DbHelper dbHelper, BluetoothSocket socket, Context context)
    {
        this.socket = socket;
        this.dbHelper = dbHelper;

        locationFinder = new LocationFinder(context);
        if (!locationFinder.canGetLocation()) locationFinder.showGPSAlert();

        liveDataArray = new ArrayList<>();

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
    @Nullable
    private synchronized String executeCommand(ObdCommand command, RESULT_FORMAT format)
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
        catch (UnableToConnectException e)
        {
//          TODO catch the unable to connect exception
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

    public boolean getStatisticalData(int tripId)
    {
//        for(ObdCommand command : availableCommands)
//            Log.i(LOG_TAG, command.getName() + ": " + executeCommand(command, RESULT_FORMAT.CALCULATED));

        try
        {
//          get statistical important data
            double load = Formatter.formatDouble(
                    executeCommand(new LoadCommand(), RESULT_FORMAT.CALCULATED));
            int rpm     = Formatter.formatInt(
                    executeCommand(new RPMCommand(), RESULT_FORMAT.CALCULATED));
            int speed   = Formatter.formatInt(
                    executeCommand(new SpeedCommand(), RESULT_FORMAT.CALCULATED));
            double throttlePosition = Formatter.formatDouble(
                    executeCommand(new ThrottlePositionCommand(), RESULT_FORMAT.CALCULATED));
            int runtime = Formatter.formatInt(
                    executeCommand(new RuntimeCommand(), RESULT_FORMAT.CALCULATED));

            dbHelper.insertCarData( load
                                  , rpm
                                  , speed
                                  , throttlePosition
                                  , runtime
                                  , tripId
                                  , locationFinder.getSpeed()
                                  , locationFinder.getLatitude()
                                  , locationFinder.getLongitude()
                                  , locationFinder.getAltitude()
                                  );

            return true;
        }
        catch(NoDataException nde)
        {
//          occurs after turing the engine off
            Log.d(LOG_TAG, "no data exception");
        }
        return false;
    }

    public boolean determineLiveData()
    {
        try
        {
            liveDataArray.clear();

            for(ObdCommand command : availableCommands)
                liveDataArray.add(new Pair<>( command.getName()
                                            , executeCommand(command, RESULT_FORMAT.FORMATTED)));

            return true;
        }
        catch(NoDataException nde)
        {
//          occurs after turing the engine off
            Log.d(LOG_TAG, "no data exception");
        }
        return false;
    }


    public void stop()
    {
        locationFinder.stopGPS();
    }

    public ArrayList<Pair<String, String>> getLiveData()
    {
        return liveDataArray;
    }
}
