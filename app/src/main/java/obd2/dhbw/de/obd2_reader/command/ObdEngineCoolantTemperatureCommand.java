package obd2.dhbw.de.obd2_reader.command;

import com.github.pires.obd.commands.temperature.TemperatureCommand;

/**
 * Created by Ricardo on 09.05.2016.
 */
public class ObdEngineCoolantTemperatureCommand
       extends TemperatureCommand
{
    public ObdEngineCoolantTemperatureCommand()
    {
        super("01 67");
    }

    @Override
    public String getName()
    {
        return "Engine coolant temperature";
    }
}
