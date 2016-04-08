package obd2.dhbw.de.obd2_reader;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.control.DistanceSinceCCCommand;
import com.github.pires.obd.commands.control.DtcNumberCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.control.TimingAdvanceCommand;
import com.github.pires.obd.commands.engine.AbsoluteLoadCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_41_60;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ricardo on 08.04.2016.
 */
public class AvailableCommands
{
//	***************************************************************************
//	CONSTANTS
//	***************************************************************************

    private static final String LOG_TAG = AvailableCommands.class.getName();

    public static enum PidArea
    {
        PIDS_01_20
        , PIDS_21_40
        , PIDS_41_60
    }

//	***************************************************************************
//	VARIABLES
//	***************************************************************************

    private static Map<String, ObdCommand> mapPidCommand = new HashMap<>();

//	***************************************************************************
//	CONSTRUCTOR
//	***************************************************************************

    static
    {
//        mapPidCommand.put("0", new AvailablePidsCommand_01_20()); // is never asked
        mapPidCommand.put("1", new DtcNumberCommand());
        mapPidCommand.put("4", new LoadCommand());
        mapPidCommand.put("a", new FuelPressureCommand());
        mapPidCommand.put("b", new IntakeManifoldPressureCommand());
        mapPidCommand.put("c", new RPMCommand());
        mapPidCommand.put("d", new SpeedCommand());
        mapPidCommand.put("e", new TimingAdvanceCommand());
        mapPidCommand.put("10", new MassAirFlowCommand());
        mapPidCommand.put("11", new ThrottlePositionCommand());
        mapPidCommand.put("1f", new RuntimeCommand());
        mapPidCommand.put("20", new AvailablePidsCommand_21_40());
        mapPidCommand.put("21", new DistanceMILOnCommand());
        mapPidCommand.put("2f", new FuelLevelCommand());
        mapPidCommand.put("31", new DistanceSinceCCCommand());
        mapPidCommand.put("33", new BarometricPressureCommand());
        mapPidCommand.put("34", new WidebandAirFuelRatioCommand());
        mapPidCommand.put("40", new AvailablePidsCommand_41_60());
        mapPidCommand.put("42", new ModuleVoltageCommand());
        mapPidCommand.put("43", new AbsoluteLoadCommand());
        mapPidCommand.put("44", new AirFuelRatioCommand());
        mapPidCommand.put("51", new FindFuelTypeCommand());
        mapPidCommand.put("5c", new OilTempCommand());
        mapPidCommand.put("5e", new ConsumptionRateCommand());
    }

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    /**
     * Check whether the string is a valid hex number.
     *
     * @param hex string to check
     *
     * @return boolean true if valid
     */
    private static boolean validateHex(String hex)
    {
        if (!hex.matches("([0-9A-F])+") || hex.length() != 8) return false;
        return true;
    }

    /**
     * Determine all support obd commands according to the answer of AvailablePidsCommand.
     *
     * @param hexString String
     * @param pidArea enumeration
     *
     * @return array list of all supported commands
     */
    public static ArrayList<ObdCommand> determineCommands(String hexString, PidArea pidArea)
    {
        if(!validateHex(hexString)) return null;

//      convert hex to binary
        String binary = Long.toBinaryString(Long.parseLong(hexString, 16));

//      array of available commands
        ArrayList<ObdCommand> commands = new ArrayList<>();

        for(int i=0; i<binary.length(); i++)
        {
//          zeros means command not supported so position useless
            if(Character.toString(binary.charAt(i)).equals("1"))
            {
//              it starts at 1 not 0
                int position = i+1;

//              add something for the different areas
                switch(pidArea)
                {
                    case PIDS_01_20:
//                      nothing to add
                        break;

                    case PIDS_21_40:
                        position += 32;
                        break;

                    case PIDS_41_60:
                        position += 64;
                        break;
                }

//              get the according command
                ObdCommand command = mapPidCommand.get(Long.toHexString(position));
                if(command != null) commands.add(command);
            }
        }

        return commands;
    }
}
