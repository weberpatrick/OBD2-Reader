package obd2.dhbw.de.obd2_reader.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.UUID;

/**
 * Created by Ricar on 06.04.2016.
 */
public class BluetoothConnector
{
    private static final String LOG_TAG = BluetoothConnector.class.getName();

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static BluetoothSocket connect(BluetoothDevice device)
    {
        BluetoothSocket socket = null;

        Log.d(LOG_TAG, "Start bluetooth connection");

        try
        {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG, "Error occurred while establishing the bluetooth connection.", e);

            //TODO check whether we need a fallback socket ?!
            return null;
        }

        return socket;
    }

}
