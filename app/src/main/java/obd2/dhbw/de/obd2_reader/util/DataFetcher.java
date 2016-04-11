package obd2.dhbw.de.obd2_reader.util;

import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
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
    private SQLiteDatabase db;

    private ArrayList<ObdCommand> availableCommands;

//	***************************************************************************
//	CONSTRUCTOR AREA
//	***************************************************************************

    public DataFetcher(DbHelper dbHelper, BluetoothSocket socket)
    {
        this.socket = socket;

        db = dbHelper.getWritableDatabase();

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

    private boolean putDataInDb( double engineLoad
                               , double intakeManifoldPressure
                               , double rpm
                               , double speed
                               , double timingAdvance
                               , double throttlePosition
                               , long runTime
                               , double barometricPressure
                               , double widebandAirFuelRatio
                               , double absoluteLoad
                               , double airFuelRatio
                               )
    {
        ContentValues values = new ContentValues();
        values.put(DbHelper.C_ENGINE_LOAD               , engineLoad            );
        values.put(DbHelper.C_INTAKE_MANIFOLD_PRESSURE  , intakeManifoldPressure);
        values.put(DbHelper.C_RPM                       , rpm                   );
        values.put(DbHelper.C_SPEED                     , speed                 );
        values.put(DbHelper.C_TIMING_ADVANCE            , timingAdvance         );
        values.put(DbHelper.C_THROTTLE_POSITION         , throttlePosition      );
        values.put(DbHelper.C_RUNTIME                   , runTime               );
        values.put(DbHelper.C_BAROMETRIC_PRESSURE       , barometricPressure    );
        values.put(DbHelper.C_WIDEBAND_AIR_FUEL_RATIO   , widebandAirFuelRatio  );
        values.put(DbHelper.C_ABSOLUTE_LOAD             , absoluteLoad          );
        values.put(DbHelper.C_AIR_FUEL_RATIO            , airFuelRatio          );

        long insertId = db.insert(DbHelper.TABLE_CAR_DATA, null, values);

        Log.d(LOG_TAG, "insert ID: " + insertId);

        if(insertId == -1) return false;

        return true;
    }

    public void start()
    {
//        for(ObdCommand command : availableCommands)
//            Log.i(LOG_TAG, command.getName() + ": " + executeCommand(command, RESULT_FORMAT.CALCULATED));

        putDataInDb( Double.parseDouble(executeCommand(new LoadCommand()                    , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new IntakeManifoldPressureCommand()  , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new RPMCommand()                     , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new SpeedCommand()                   , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new TimingAdvanceCommand()           , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new ThrottlePositionCommand()        , RESULT_FORMAT.CALCULATED))
                       , Integer.parseInt(  executeCommand(new RuntimeCommand()                 , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new BarometricPressureCommand()      , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new WidebandAirFuelRatioCommand()    , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new AbsoluteLoadCommand()            , RESULT_FORMAT.CALCULATED))
                       , Double.parseDouble(executeCommand(new AirFuelRatioCommand()            , RESULT_FORMAT.CALCULATED))
            );
    }
}
