package obd2.dhbw.de.obd2_reader;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import obd2.dhbw.de.obd2_reader.connection.BluetoothConnector;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;
import obd2.dhbw.de.obd2_reader.util.DataFetcher;

public class MainActivity
        extends AppCompatActivity
{
//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = MainActivity.class.getName();

    private final int BLUETOOTH_REQUEST = 1;

//	***************************************************************************
//	DECLARATION OF VARIABLES
//	***************************************************************************

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    private ArrayList<BluetoothDevice> pairedDevices;

    private BluetoothSocket socket;
    private BluetoothAdapter btAdapter;

    private boolean bluetoothEnabled;

    private Thread dataFetcherThread;

//	***************************************************************************
//	gui components
//	***************************************************************************

    private Button buttonStartStop;
    private ScrollView scrollViewData;

    private TextView textViewSpeedValue;
    private TextView textViewRpmValue;
    private TextView textViewRuntime;

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
        db = dbHelper.getReadableDatabase();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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

//	***************************************************************************
//	private methods
//	***************************************************************************

    private void initComponents()
    {
        scrollViewData = (ScrollView) findViewById(R.id.scrollViewData);

        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//              initialize bluetooth adapter and turn it on
                initBluetoothAdapter();

                showAdapterSelectionDialog();
            }
        });

        textViewSpeedValue                  = (TextView) findViewById(R.id.textViewSpeedValue);
        textViewRpmValue                    = (TextView) findViewById(R.id.textViewRpmValue);
        textViewRuntime                     = (TextView) findViewById(R.id.textViewRuntimeValue);

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
        pairedDevices = new ArrayList<BluetoothDevice>();

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
        }else
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
//      declare cursor to read data
//        TODO get data in descading order
        Cursor cursor = db.query(DbHelper.TABLE_CAR_DATA
                , null //columns
                , null //DbHelper.C_ID +"=1" //where clause
                , null //selectionArgs
                , null //groupBy
                , null //having
                , DbHelper.C_ID + " DESC" //order by
                , "1" //limit
        );

        Log.d(LOG_TAG, "cursor length: " + cursor.getCount());

        if(cursor.moveToPosition(0))
        {
            Log.d(LOG_TAG, String.valueOf(cursor.getInt(0)));
            Log.d(LOG_TAG, cursor.getString(1));

            textViewEngineLoad.setText(String.valueOf(cursor.getDouble(2)));

            textViewIntakeManifoldPressure.setText(String.valueOf(cursor.getDouble(3)));
            textViewRpmValue.setText(String.valueOf(cursor.getDouble(4)));

            textViewSpeedValue.setText(String.valueOf(cursor.getDouble(5)));
            textViewTimingAdvanceValue.setText(String.valueOf(cursor.getDouble(6)));
            textViewThrottlePositionValue.setText(String.valueOf(cursor.getDouble(7)));
            textViewRuntime.setText(String.valueOf(cursor.getInt(8)));
            textViewBarometricPressureValue.setText(String.valueOf(cursor.getDouble(9)));
            textViewWidebandAirFuelRatioValue.setText(String.valueOf(cursor.getDouble(10)));
            textViewAbsoluteLoadValue.setText(String.valueOf(cursor.getDouble(11)));
            textViewAirFuelRatioValue.setText(String.valueOf(cursor.getDouble(12)));
        }

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void startLiveData()
    {
        dataFetcherThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DataFetcher dataFetcher = new DataFetcher(dbHelper, socket);
                dataFetcher.start();
            }
        });
        dataFetcherThread.start();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                fetchData();
            }
        }).start();
    }
}
