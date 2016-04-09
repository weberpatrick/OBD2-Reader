package obd2.dhbw.de.obd2_reader;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
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

    public SQLiteDatabase db;

    private ArrayList<BluetoothDevice> pairedDevices;

    private BluetoothSocket socket;
    private BluetoothAdapter btAdapter;

    private Button buttonStartStop;

    private boolean bluetoothEnabled;

    private Thread dataFetcherThread;

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

    private void database()
    {
//      create the database
        DbHelper dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

//        dbHelper.dropTable(db);

//      put some test stuff in it
        for (int i=0; i<10; i++)
        {
            ContentValues values = new ContentValues();
            values.put(DbHelper.COLUMN_NAME, "test" + i);
            db.insert(DbHelper.TABLE_NAME, null, values);
        }

//      declare cursor to read data
        Cursor cursor = db.query(DbHelper.TABLE_NAME
                , new String[]{DbHelper.COLUMN_NAME} //columns
                , null //DbHelper.COLUMN_ID +"=1" //where clause
                , null
                , null
                , null
                , null
        );

        Log.d(LOG_TAG, "cursor length: " + cursor.getCount());

//      start cursor
        for(int i = 0; i < cursor.getCount(); i++)
        {
            if(cursor.moveToPosition(i)) Log.d(LOG_TAG, cursor.getString(0));
        }
    }

    private void startLiveData()
    {
        dataFetcherThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DataFetcher dataFetcher = new DataFetcher(socket);
                dataFetcher.start();
            }
        });
        dataFetcherThread.start();
    }
}
