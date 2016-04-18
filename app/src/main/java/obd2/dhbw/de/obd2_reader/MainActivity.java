package obd2.dhbw.de.obd2_reader;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import obd2.dhbw.de.obd2_reader.connection.BluetoothConnector;
import obd2.dhbw.de.obd2_reader.container.DataRow;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;
import obd2.dhbw.de.obd2_reader.util.InputDataReader;
import obd2.dhbw.de.obd2_reader.util.TripCalculator;

public class MainActivity
        extends AppCompatActivity
{
//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = MainActivity.class.getName();

    private final int BLUETOOTH_REQUEST   = 1;

    private final int INPUT_DATA_INTERVAL = 1000;
    private final int READ_DATA_INTERVAL  = 1000;

//	***************************************************************************
//	DECLARATION OF VARIABLES
//	***************************************************************************

    private DbHelper dbHelper;

    private BluetoothSocket socket;
    private BluetoothAdapter btAdapter;

    private boolean bluetoothEnabled;
    private boolean isRunning;

    private InputDataReader inputDataReader;

    private Timer timerInputDataReader;

    private int tripId;

//	***************************************************************************
//	gui components
//	***************************************************************************

    private Button buttonStartStop;

    private ImageView imageViewCompass;

    private TextView textViewSpeedValue;
    private TextView textViewRpmValue;
    private TextView textViewRuntimeValue;

    private TextView textViewEngineLoad;
    private TextView textViewIntakeManifoldPressure;
    private TextView textViewTimingAdvanceValue;
    private TextView textViewThrottlePositionValue;
    private TextView textViewBarometricPressureValue;
    private TextView textViewWidebandAirFuelRatioValue;
    private TextView textViewAbsoluteLoadValue;
    private TextView textViewAirFuelRatioValue;

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//      create the database if necessary
        dbHelper = new DbHelper(this);

        initComponents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Is called after the start bluetooth intent.
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == BLUETOOTH_REQUEST && resultCode == RESULT_OK)
        {
            Log.d(LOG_TAG, "Bluetooth enabled");
            bluetoothEnabled = true;
            showAdapterSelectionDialog();
        }
        else
        {
            Log.d(LOG_TAG, "User don´t want to enable bluetooth.");
            bluetoothEnabled = false;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d(LOG_TAG, "start");
    }

    protected void onRestart()
    {
        super.onRestart();
        Log.d(LOG_TAG, "restart");
    }

    protected void onResume()
    {
        super.onResume();
        Log.d(LOG_TAG, "resume");
    }

    protected void onPause()
    {
        super.onPause();
        Log.d(LOG_TAG, "pause");
    }

    protected void onStop()
    {
        super.onStop();
        Log.d(LOG_TAG, "stop");
    }

    protected void onDestroy()
    {
        super.onDestroy();

        endTrip();
    }

//	***************************************************************************
//	private methods
//	***************************************************************************

    private void initComponents()
    {
        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//              recording data is running
                if(isRunning)
                {
                    buttonStartStop.setText(R.string.buttonStop);

                    isRunning = false;
                }
//              start of a new trip
                else
                {
                    buttonStartStop.setText(R.string.buttonStart);

//                  initialize bluetooth adapter and turn it on
                    initBluetoothAdapter();

                    showAdapterSelectionDialog();
                }

            }
        });

        imageViewCompass                    = (ImageView) findViewById(R.id.imageViewCompass);

        textViewSpeedValue                  = (TextView) findViewById(R.id.textViewSpeedValue);
        textViewRpmValue                    = (TextView) findViewById(R.id.textViewRpmValue);
        textViewRuntimeValue                = (TextView) findViewById(R.id.textViewRuntimeValue);

        textViewEngineLoad                  = (TextView) findViewById(R.id.textViewEngineLoadValue);
        textViewIntakeManifoldPressure      = (TextView) findViewById(R.id.textViewIntakeManifoldPressureValue);
        textViewTimingAdvanceValue          = (TextView) findViewById(R.id.textViewTimingAdvanceValue);
        textViewThrottlePositionValue       = (TextView) findViewById(R.id.textViewThrottlePositionValue);
        textViewBarometricPressureValue     = (TextView) findViewById(R.id.textViewBarometricPressureValue);
        textViewWidebandAirFuelRatioValue   = (TextView) findViewById(R.id.textViewWidebandAirFuelRatioValue);
        textViewAbsoluteLoadValue           = (TextView) findViewById(R.id.textViewAbsoluteLoadValue);
        textViewAirFuelRatioValue           = (TextView) findViewById(R.id.textViewAirFuelRatioValue);
    }

    /**
     * Get the default bluetooth adapter and enable it if necessary.
     */
    private void initBluetoothAdapter()
    {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null)
        {
            Toast.makeText( getApplicationContext()
                          , "No bluetooth adapter detected."
                          , Toast.LENGTH_SHORT
                          ).show();
            finish();
        }
        else if(!btAdapter.isEnabled())
        {
            bluetoothEnabled = false;

//          enable the bluetooth adapter
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST);
        }
        else
        {
            bluetoothEnabled = true;
        }
    }

    private void showAdapterSelectionDialog()
    {
        if(bluetoothEnabled)
        {
            Set<BluetoothDevice> devicesSet = btAdapter.getBondedDevices();
            final ArrayList<BluetoothDevice> deviceArray = new ArrayList<>(devicesSet);

            CharSequence[] deviceNames = new String[deviceArray.size()];

            for(int i=0; i<deviceArray.size(); i++)
                deviceNames[i] = deviceArray.get(i).getName();


            if(devicesSet.size() > 0)
            {
                Log.d(LOG_TAG, "showAdapterSelectionDialog");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.adapterSelectionDialogCaption);
                builder.setItems(deviceNames, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(createConnection(deviceArray.get(which)))
                        {
                            Log.d(LOG_TAG, "Befor starting live data");
                            startLiveData();
                        }
                    }
                });
                builder.create().show();
            }
            else
            {
                Toast.makeText( getApplicationContext()
                        , "No paired device detected"
                        , Toast.LENGTH_SHORT
                ).show();
            }
        }

    }

    /**
     * Create a connection to the delivered bluetooth device.
     *
     * @param device bluetooth device
     *
     * @return bluetooth socket or null
     */
    private boolean createConnection(BluetoothDevice device)
    {
        if(device == null)
        {
            Log.e(LOG_TAG, "The device should not be null here!");
            return false;
        }
        else
        {
            socket = BluetoothConnector.connect(device);
        }

        if(socket == null)
        {
            Toast.makeText( getApplicationContext()
                    , "Connection failed, try again."
                    , Toast.LENGTH_SHORT
            ).show();
            return false;
        }
        return true;
    }

    private void fetchData()
    {
        DataRow dataRow = dbHelper.selectCarData(tripId);

//        Log.d(LOG_TAG, dataRow.getTimestamp());

        if(dataRow != null)
        {
            updateTextView(textViewEngineLoad               , dataRow.getEngineLoadString());

            updateTextView(textViewIntakeManifoldPressure   , dataRow.getIntakeManifoldPressureString());
            updateTextView(textViewRpmValue                 , dataRow.getRpmString());
            updateTextView(textViewSpeedValue               , dataRow.getSpeedString());
            updateTextView(textViewTimingAdvanceValue       , dataRow.getTimingAdvanceString());
            updateTextView(textViewThrottlePositionValue    , dataRow.getThrottlePositionString());
            updateTextView(textViewRuntimeValue             , dataRow.getRunTimeString());
            updateTextView(textViewBarometricPressureValue  , dataRow.getBarometricPressureString());
            updateTextView(textViewWidebandAirFuelRatioValue, dataRow.getWidebandAirFuelRatioString());
            updateTextView(textViewAbsoluteLoadValue        , dataRow.getAbsoluteLoadString());
            updateTextView(textViewAirFuelRatioValue        , dataRow.getAirFuelRatioString());
        }
    }

    private void startLiveData()
    {
        inputDataReader = new InputDataReader(dbHelper, socket, this);

        tripId = dbHelper.getLatestTripId() + 1;

        timerInputDataReader = new Timer();
        timerInputDataReader.schedule(new TaskInputDataReader(), 0);

        Timer timerReadData = new Timer();
        timerReadData.schedule(new TaskReadData(), 5000);
    }

    private class TaskInputDataReader
            extends TimerTask
    {
        @Override
        public void run()
        {
            Log.d(LOG_TAG, "TaskInputDataReader");
            if(inputDataReader.start(tripId) && isRunning)
                timerInputDataReader.schedule( new TaskInputDataReader()
                                             , INPUT_DATA_INTERVAL);
            else endTrip();
        }
    }

    private class TaskReadData
            extends TimerTask
    {
        @Override
        public void run()
        {
            Log.d(LOG_TAG, "TaskReadData");
            fetchData();
            if(isRunning) timerInputDataReader.schedule( new TaskReadData()
                        , READ_DATA_INTERVAL);
        }
    }

    private void endTrip()
    {
        TripCalculator.calculate(dbHelper, tripId);

//      stop gps stuff
        inputDataReader.stop();
    }

    public void updateTextView(final TextView view, final String txt)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            public void run()
            {
                view.setText(txt);
            }
        });
    }
}
