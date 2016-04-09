package obd2.dhbw.de.obd2_reader.util;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
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
public class DataFetcher
{
//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = DataFetcher.class.getName();

    private enum RESULT_FORMAT
    {
        NO_RESULT
        , RAW
        , CALCULATED
        , FORMATTED
    }

//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private BluetoothSocket socket;
    private DbHelper dbHelper;

    private ArrayList<ObdCommand> availableCommands;

//	***************************************************************************
//	CONSTRUCTOR AREA
//	***************************************************************************

    public DataFetcher(DbHelper dbHelper, BluetoothSocket socket)
    {
        this.dbHelper = dbHelper;
        this.socket = socket;
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
            Log.d(LOG_TAG, "obd reset");
            executeCommand(new ObdResetCommand(), RESULT_FORMAT.NO_RESULT);

            Log.d(LOG_TAG, "echo Off");
            executeCommand(new EchoOffCommand(), RESULT_FORMAT.NO_RESULT);

            Log.d(LOG_TAG, "line feed Off");
            executeCommand(new LineFeedOffCommand(), RESULT_FORMAT.NO_RESULT);

            Log.d(LOG_TAG, "time out");
            executeCommand(new TimeoutCommand(125), RESULT_FORMAT.NO_RESULT);

            Log.d(LOG_TAG, "protocol auto");
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

    private void determineAvailablePids()
    {
        availableCommands = new ArrayList<>();

        String hex_01_20 = executeCommand(new AvailablePidsCommand_01_20(), RESULT_FORMAT.FORMATTED);
        String hex_21_40 = executeCommand(new AvailablePidsCommand_21_40(), RESULT_FORMAT.FORMATTED);
        String hex_41_60 = executeCommand(new AvailablePidsCommand_41_60(), RESULT_FORMAT.FORMATTED);

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

    private boolean putDataToDb()
    {

        return true;
    }

    public void start()
    {
        initOdb();

        determineAvailablePids();

        for(ObdCommand command : availableCommands)
            Log.i(LOG_TAG, command.getName() + ": " + executeCommand(command, RESULT_FORMAT.CALCULATED));

//        while(!Thread.currentThread().isInterrupted())
//        {
//            Log.i(LOG_TAG, executeCommand(new RPMCommand(), true));
//            Log.i(LOG_TAG, executeCommand(new SpeedCommand(), true));
//            Log.i(LOG_TAG, executeCommand(new RuntimeCommand(), true));
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
