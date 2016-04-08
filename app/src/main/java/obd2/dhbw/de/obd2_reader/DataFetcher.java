package obd2.dhbw.de.obd2_reader;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
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

/**
 * Created by Ricar on 08.04.2016.
 */
public class DataFetcher
{
//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = DataFetcher.class.getName();

//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private BluetoothSocket socket;

//	***************************************************************************
//	CONSTRUCTOR AREA
//	***************************************************************************

    public DataFetcher(BluetoothSocket socket)
    {
        this.socket = socket;
    }

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    /**
     * Start a new thread to initialize the obd adapter.
     */
    private void initOdb()
    {
        Log.d(LOG_TAG, "initOdb");

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.d(LOG_TAG, "obd reset");
                    executeCommand(new ObdResetCommand());

                    Log.d(LOG_TAG, "echo Off");
                    executeCommand(new EchoOffCommand());

                    Log.d(LOG_TAG, "echo Off");
                    executeCommand(new EchoOffCommand());

                    Log.d(LOG_TAG, "line feed Off");
                    executeCommand(new LineFeedOffCommand());

                    Log.d(LOG_TAG, "time out");
                    executeCommand(new TimeoutCommand(125));

                    Log.d(LOG_TAG, "protocol auto");
                    executeCommand(new SelectProtocolCommand(ObdProtocols.AUTO));
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG, "exception " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Executes the delivered command.
     *
     * @param command ObdCommand to be executed
     */
    private String executeCommand(ObdCommand command)
    {
        try
        {
//          TODO when is the socket not connected ?!
            if(socket.isConnected())
            {
                command.run(socket.getInputStream(), socket.getOutputStream());
                return command.getFormattedResult();
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

    private void showAvailablePids()
    {
        Log.d(LOG_TAG, executeCommand(new AvailablePidsCommand_01_20()));
        Log.d(LOG_TAG, executeCommand(new AvailablePidsCommand_21_40()));
        Log.d(LOG_TAG, executeCommand(new AvailablePidsCommand_41_60()));
    }

    public void start()
    {
        initOdb();

        showAvailablePids();

        while(!Thread.currentThread().isInterrupted())
        {
            Log.i(LOG_TAG, executeCommand(new RPMCommand()));
            Log.i(LOG_TAG, executeCommand(new SpeedCommand()));
            Log.i(LOG_TAG, executeCommand(new RuntimeCommand()));
        }
    }
}
