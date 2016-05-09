package obd2.dhbw.de.obd2_reader;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import obd2.dhbw.de.obd2_reader.connection.BluetoothConnector;
import obd2.dhbw.de.obd2_reader.container.DataRow;
import obd2.dhbw.de.obd2_reader.container.TripRow;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;
import obd2.dhbw.de.obd2_reader.util.AdapterAgent;
import obd2.dhbw.de.obd2_reader.util.Compass;
import obd2.dhbw.de.obd2_reader.util.TripCalculator;

public class MainActivity
       extends AppCompatActivity
{
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

    private int currentTripId;

    private List<String[]> tripStringArray = new ArrayList<>();

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

    private ListView drawerList;
    private ArrayAdapter<String[]> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private String title;

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

        dbHelper.insertTripData(1, "01.01.2015", 234.4, 60, 5.0, 120.0, 75.0);
        dbHelper.insertTripData(2, "01.01.2015", 3453.6, 3600, 75.0, 220.0, 115.0);
        dbHelper.insertTripData(3, "01.01.2015", 14.4, 2345, 43.0, 20.0, 75.0);
        dbHelper.insertTripData(6, "01.01.2015", 2464.7, 12, 1.0, 100.0, 45.0);
        dbHelper.insertTripData(8, "01.01.2015", 265.0, 345, 12.0, 750.0, 25.0);

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
        //close the drawer
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
        endTrip();super.onDestroy();


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

        drawerList = (ListView)findViewById(R.id.drawerList);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        title = getTitle().toString();
        addDrawerItems();
    }

    /**
     * Init the left drawer with trips
     */
    private void addDrawerItems()
    {
        int[] tripIdArray = dbHelper.getTripIds();

        if (tripIdArray.length>0)
        {
            for (int i : tripIdArray){
                tripStringArray.add(new String[] {"Trip ", String.valueOf(i), "(" + dbHelper.selectTrip(i).getDate() + ")"});
            }
        }else{
            // No trips found
            tripStringArray.add(new String[] {getString(R.string.noTripsFound), "", ""});
        }

        drawerAdapter = new ArrayAdapter<String[]>(this, R.layout.drawer_row, tripStringArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // 1. Create inflater
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                // 2. Get view from inflater
                View view = inflater.inflate(R.layout.drawer_row, parent, false);

                // 3. Get the textViews from the view
                TextView tripText = (TextView) view.findViewById(R.id.drawerRowTrip);
                TextView tripId   = (TextView) view.findViewById(R.id.drawerRowId);
                TextView tripDate = (TextView) view.findViewById(R.id.drawerRowDate);

                // 4. Set the text for textViews
                String[] row = tripStringArray.get(position);
                tripText.setText(row[0]);
                tripId.setText(row[1]);
                tripDate.setText(row[2]);

                // 5. return view
                return view;
            }

        };
        drawerList.setAdapter(drawerAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ViewGroup group = (ViewGroup) view;
                TextView tripId = (TextView) group.findViewById(R.id.drawerRowId);

                showTripMessage(Integer.parseInt(tripId.getText().toString()));

            }
        });

        //Icon in the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setupDrawer();
    }

    /**
     * init behavior of drawer
     */
    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.openDrawer, R.string.closeDrawer) {

            /** Called when a drawer is completely opened. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Trips");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer is completely closed. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
    }

    /*
    * in case a new Trip was made, the list in the Drawer is refreshed
    */
    private void refreshDrawer()
    {
        String date = "";
        TripRow row = dbHelper.selectTrip(currentTripId);

        //If the device has no Bluetooth, and the user hits the start Button,
        //this here is going to be called and would throw an Exception
        if (row != null)
        date = row.getDate();

        //add "Trip ", id, date to the top of the List and update the listAdapter
        tripStringArray.add(0, new String[] {"Trip ", String.valueOf(currentTripId),"(" +  date + ")"});

        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                drawerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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

        currentTripId = dbHelper.getLatestTripId() + 1;

        timerAdapterAgent = new Timer();
        timerAdapterAgent.schedule(new TaskAdapterAgent(), 0);

        timerPresenter = new Timer();
        timerPresenter.schedule(new TaskPresenter(), 0);

        Timer timerCompass = new Timer();
        timerCompass.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                updateImageView(imageViewCompass, compass.getLastRotation(), compass.getRotation());
            }
        }, 0, 1000);
    }

    private void presentData()
    {
//      statistical important data
        DataRow dataRow = dbHelper.selectCarData(currentTripId);

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
            if(adapterAgent.getStatisticalData(currentTripId)
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
        if(dbHelper != null) TripCalculator.calculate(dbHelper, currentTripId, INPUT_DATA_INTERVAL);

        refreshDrawer();

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

        showTripMessage(currentTripId);
    }

    /**
     * Show the AlertDialog for a given trip
     */
    private void showTripMessage(final int tripId) {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                TripRow tripRow = dbHelper.selectTrip(tripId);

                if(tripRow != null)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.tripAlertCaption);
                    builder.setMessage(
                            "Distanz: " + tripRow.getDistance() + "\n"
                                    + "Maximalgeschwindigkeit: " + tripRow.getMaxSpeed() + "\n"
                                    + "Durchschnittsgeschwindigkeit: " + tripRow.getAvgSpeed() + "\n"
                                    + "Fahrzeit: " + tripRow.getRunTime() / 60 + " Minuten " + tripRow.getRunTime() % 60 + " Sekunden" + "\n"
                                    + "Stehzeit: " + tripRow.getStandTime() / 60 + " Minuten " + tripRow.getRunTime() % 60 + " Sekunden"
                    );
                    builder.setPositiveButton("Ok", null);

                    builder.create().show();
                }
            }
        });
    }
}