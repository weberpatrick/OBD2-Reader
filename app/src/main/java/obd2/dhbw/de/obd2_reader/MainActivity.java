package obd2.dhbw.de.obd2_reader;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import obd2.dhbw.de.obd2_reader.connection.BluetoothConnector;
import obd2.dhbw.de.obd2_reader.container.DataRow;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;
import obd2.dhbw.de.obd2_reader.util.AdapterAgent;
import obd2.dhbw.de.obd2_reader.util.Compass;
import obd2.dhbw.de.obd2_reader.util.TripCalculator;

public class MainActivity
       extends AppCompatActivity
{
//  TODO http://mindtherobot.com/blog/272/android-custom-ui-making-a-vintage-thermometer/

//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = MainActivity.class.getName();

    private final int BLUETOOTH_REQUEST   = 1;

    private final int INPUT_DATA_INTERVAL = 100;
    private final int PRESENTER_INTERVAL  = 100;

//	***************************************************************************
//	DECLARATION OF VARIABLES
//	***************************************************************************

    private DbHelper dbHelper;

    private BluetoothSocket socket;
    private BluetoothAdapter btAdapter;

    private boolean bluetoothEnabled;
    private boolean isRunning;

    private AdapterAgent adapterAgent;

    private Compass compass;

    private Timer timerAdapterAgent;
    private Timer timerPresenter;

    private int tripId;

    private Map<String, TextView> mapLiveTextViews;

//	***************************************************************************
//	gui components
//	***************************************************************************

    private TableLayout tableLayoutData;
    private Button buttonStartStop;

    private ImageView imageViewCompass;

    private TextView textViewSpeedValue;
    private TextView textViewRpmValue;
    private TextView textViewRuntimeValue;

    private TextView textViewEngineLoadValue;
    private TextView textViewThrottlePositionValue;

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

        compass = new Compass(this);

        mapLiveTextViews = new HashMap<>();

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
     * Is called after the getStatisticalData bluetooth intent.
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
    }

    protected void onRestart()
    {
        super.onRestart();
    }

    protected void onResume()
    {
        super.onResume();
    }

    protected void onPause()
    {
        super.onPause();
    }

    protected void onStop()
    {
        super.onStop();
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
                    buttonStartStop.setText(R.string.buttonStart);

                    isRunning = false;
                }
                else
                {
                    buttonStartStop.setText(R.string.buttonStop);

                    isRunning = true;

//                  initialize bluetooth adapter and turn it on
                    initBluetoothAdapter();

                    showAdapterSelectionDialog();
                }

            }
        });

        tableLayoutData                 = (TableLayout) findViewById(R.id.tableLayoutData);
        imageViewCompass                = (ImageView) findViewById(R.id.imageViewCompass);

        textViewSpeedValue              = (TextView) findViewById(R.id.textViewSpeedValue);
        textViewRpmValue                = (TextView) findViewById(R.id.textViewRpmValue);
        textViewRuntimeValue            = (TextView) findViewById(R.id.textViewRuntimeValue);

        textViewEngineLoadValue         = (TextView) findViewById(R.id.textViewEngineLoadValue);
        textViewThrottlePositionValue   = (TextView) findViewById(R.id.textViewThrottlePositionValue);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.adapterSelectionDialogCaption);
                builder.setItems(deviceNames, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(createConnection(deviceArray.get(which))) startLiveData();
                        else
                        {
                            buttonStartStop.setText(R.string.buttonStart);
                            isRunning = false;
                        }
                    }
                });
                builder.create().show();
            }
            else Toast.makeText( getApplicationContext()
                               , "No paired device detected"
                               , Toast.LENGTH_SHORT
                               ).show();
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
        else socket = BluetoothConnector.connect(device);

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

    private void startLiveData()
    {
        adapterAgent = new AdapterAgent(dbHelper, socket, this);

        tripId = dbHelper.getLatestTripId() + 1;

        timerAdapterAgent = new Timer();
        timerAdapterAgent.schedule(new TaskAdapterAgent(), 0);

        timerPresenter = new Timer();
        timerPresenter.schedule(new TaskPresenter(), 0);
    }

    private void presentData()
    {
//      statistical important data
        DataRow dataRow = dbHelper.selectCarData(tripId);

//        Log.d(LOG_TAG, dataRow.getTimestamp());

        if(dataRow != null)
        {
            updateTextView(textViewEngineLoadValue          , dataRow.getEngineLoadString());
            updateTextView(textViewRpmValue                 , dataRow.getRpmString());
            updateTextView(textViewSpeedValue               , dataRow.getSpeedString());
            updateTextView(textViewThrottlePositionValue    , dataRow.getThrottlePositionString());
            updateTextView(textViewRuntimeValue             , dataRow.getRunTimeString());
        }

//      live data
        for(Pair<String, String> pair : new ArrayList<>(adapterAgent.getLiveData()))
                refreshTextView(pair.first, pair.second);

        updateImageView(imageViewCompass, compass.getLastRotation(), compass.getRotation());
    }

    private void refreshTextView(final String name, final String value)
    {
        TextView textView = mapLiveTextViews.get(name);

        if(textView != null)  updateTextView(textView, value);
        else
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                public void run()
                {
                    TableRow tableRow       = new TableRow(getApplicationContext());
                    TextView textViewName   = new TextView(getApplicationContext());
                    TextView textViewValue  = new TextView(getApplicationContext());

                    textViewName.setTextColor(Color.BLACK);
                    textViewName.setText(name);
                    textViewValue.setTextColor(Color.BLACK);
                    textViewValue.setText(value);
                    textViewValue.setGravity(Gravity.RIGHT);

                    tableRow.addView(textViewName);
                    tableRow.addView(textViewValue);

                    mapLiveTextViews.put(name, textViewValue);
                    tableLayoutData.addView(tableRow);
                }
            });
        }
    }

    private void updateTextView(final TextView view, final String txt)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            public void run()
            {
                view.setText(txt);
            }
        });
    }

    private void updateImageView(final ImageView image, final float lastRotation, final float rotation)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            public void run()
            {
                float newRotation = rotation;
                float newLastRotation = lastRotation;

                if ((rotation>0 && rotation<90) && (lastRotation>270 && lastRotation<360)){
                    newRotation = rotation + 360;
                }else if ((lastRotation>0 && lastRotation<90) && (rotation>270 && rotation<360)){
                    newLastRotation = lastRotation + 360;
                }

                Animation an = new RotateAnimation(-newLastRotation, -newRotation,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                        0.5f);

                an.setDuration(PRESENTER_INTERVAL);
                an.setRepeatCount(0);
                an.setFillAfter(true);

                image.startAnimation(an);
            }
        });
    }

    private class TaskAdapterAgent
            extends TimerTask
    {
        @Override
        public void run()
        {
            if(adapterAgent.getStatisticalData(tripId)
                    && adapterAgent.determineLiveData()
                    && isRunning)
                timerAdapterAgent.schedule( new TaskAdapterAgent()
                                          , INPUT_DATA_INTERVAL);
            else endTrip();
        }
    }

    private class TaskPresenter
            extends TimerTask
    {
        @Override
        public void run()
        {
            presentData();
            if(isRunning) timerPresenter.schedule(new TaskPresenter(), PRESENTER_INTERVAL);
        }
    }

    private void endTrip()
    {
        if(dbHelper != null) TripCalculator.calculate(dbHelper, tripId);

//      stop gps stuff
        if(adapterAgent != null) adapterAgent.stop();

        if(compass != null) compass.stop();

        try
        {
            if(socket != null) socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
