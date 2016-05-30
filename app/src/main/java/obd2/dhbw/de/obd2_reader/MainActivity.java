package obd2.dhbw.de.obd2_reader;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import obd2.dhbw.de.obd2_reader.connection.BluetoothConnector;
import obd2.dhbw.de.obd2_reader.container.DataRow;
import obd2.dhbw.de.obd2_reader.container.TripRow;
import obd2.dhbw.de.obd2_reader.dialog.InfoDialog;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;
import obd2.dhbw.de.obd2_reader.util.AdapterAgent;
import obd2.dhbw.de.obd2_reader.util.Compass;
import obd2.dhbw.de.obd2_reader.util.LocationFinder;
import obd2.dhbw.de.obd2_reader.util.TripCalculator;

public class MainActivity
       extends AppCompatActivity
{
//	***************************************************************************
//	DECLARATION OF CONSTANTS
//	***************************************************************************

    private final String LOG_TAG = MainActivity.class.getName();

    private final int BLUETOOTH_REQUEST = 1;

    private final int INPUT_DATA_DELAY = 0;
    private final int PRESENTER_DELAY  = 300;
    private final int COMPASS_DELAY    = 1000;

//	***************************************************************************
//	DECLARATION OF VARIABLES
//	***************************************************************************

    private DbHelper dbHelper;

    private BluetoothSocket socket;
    private BluetoothAdapter btAdapter;

    private boolean bluetoothEnabled;
    private boolean isRunning = false;

    private AdapterAgent adapterAgent;

    private Compass compass;
    private LocationFinder locationFinder;

    private Timer timerAdapterAgent;
    private Timer timerPresenter;

    private int currentTripId;

    private List<String[]> tripStringArray = new ArrayList<>();
    private String[] noTripsString;

    private Map<String, TextView> mapLiveTextViews;

    private ListView drawerList;
    private ArrayAdapter<String[]> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    private String title;

    private TripRow deletedTripRow;
    private int indexOfRowToDelete;

    private boolean isStanding = false;
    private long startStandTime = 0;
    private long standTime = 0;
    private long drivingTime = 0;

//	***************************************************************************
//	gui components
//	***************************************************************************

    private TableLayout tableLayoutData;
    private ImageButton buttonStartStop;

    private ImageView imageViewCompass;

    private TextView textViewSpeedValue;
    private TextView textViewRpmValue;
    private TextView textViewRuntimeValue;
    private TextView textViewEngineLoadValue;

    private ProgressBar progressBarThrottlePosition;
    private ProgressBar progressBarStandTime;

    private Chronometer chronometer;

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

        //Don#t let the screen rotate, just PORTRAIT mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//      create the database if necessary
        dbHelper = new DbHelper(this);

        dbHelper.insertTripData(1, "01.01.2015", 234.4, 60, 5, 120.0, 75.0, "Mannheim");
        dbHelper.insertTripData(2, "01.01.2015", 3453.6, 3600, 75, 220.0, 115.0, "Görlitz");
        dbHelper.insertTripData(3, "01.01.2015", 14.4, 2345, 43, 20.0, 75.0, "Heusenstamm");
        dbHelper.insertTripData(6, "01.01.2015", 2464.7, 12, 1, 100.0, 45.0, "Berlin");
        dbHelper.insertTripData(8, "01.01.2015", 265.0, 345, 12, 750.0, 25.0, "Frankfurt");

        mapLiveTextViews = new HashMap<>();

        initComponents();
        noTripsString = new String[] {getString(R.string.noTripsFound), "", ""};
        compass = new Compass(this);
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                updateImageView( imageViewCompass
                        , compass.getLastRotation()
                        , compass.getRotation()
                );
            }
        }, 0, COMPASS_DELAY);

        locationFinder = new LocationFinder(this);
        if (!locationFinder.startGps()) locationFinder.showGPSAlert();
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
        if (id == R.id.parkPosition)
        {
            SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);

            //99 is returned, if pref does not exist
            double latitude  = Double.longBitsToDouble(prefs.getLong(getString(R.string.pref_latitude), Double.doubleToLongBits(99)));
            double longitude = Double.longBitsToDouble(prefs.getLong(getString(R.string.pref_longitude), Double.doubleToLongBits(99)));

            if (latitude == 99 || longitude == 99)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        InfoDialog.show(MainActivity.this, getString(R.string.parkPosition), getString(R.string.parkPositionNotFound));
                    }
                });
            }
            else
            {
                //first 2 parameters:       center camera to this position
                //z = 17:                   zoom level (0:whole world, 25:closest)
                //next 2 doubles an string: put a mapMarker on the map
                String uri = String.format(Locale.ENGLISH, "geo:%f, %f?z=17&q=%f, %f(%s)"
                        , latitude, longitude, latitude, longitude, getResources().getString(R.string.parkPosition));
                //start Intent to google maps or other Activity that can show "geo..."
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
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

    /**
     * If Drawer is opened, close the drawer
     * else ask user, if he really wants to close app
     */
    @Override
    public void onBackPressed()
    {
        if(drawerLayout.isDrawerOpen(drawerList))
        {
            drawerLayout.closeDrawer(drawerList);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.closeAppQuestion);
            builder.setPositiveButton(R.string.closeYes, new  DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();
                }
            });
            builder.setNegativeButton(R.string.closeNo, null);
            builder.create().show();
        }
    }

    @Override
    protected void onStart()
    {
        if(compass != null) compass.registerListener();

        //keep screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        if(compass != null) compass.unregisterListener();

        //clears the keep screen awake feature
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onStop();
    }

    protected void onDestroy()
    {
        endStuff();
        super.onDestroy();
    }

//	***************************************************************************
//	private methods
//	***************************************************************************

    private void initComponents()
    {
        buttonStartStop = (ImageButton) findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//              recording data is running
                if(isRunning) isRunning = false;
                else
                {
                    if (locationFinder.canGetLocation())
                    {
                        //initialize bluetooth adapter and turn it on
                        initBluetoothAdapter();

                        showAdapterSelectionDialog();
                    }
                    else locationFinder.showGPSAlert();
                }
            }
        });

        progressBarThrottlePosition = (ProgressBar) findViewById(R.id.progressBarThrottle);
        progressBarStandTime        = (ProgressBar) findViewById(R.id.progressBarStandTime);

        tableLayoutData      = (TableLayout) findViewById(R.id.tableLayoutData);
        imageViewCompass     = (ImageView) findViewById(R.id.imageViewCompass);

        textViewSpeedValue   = (TextView) findViewById(R.id.textViewSpeedValue);
        textViewRpmValue     = (TextView) findViewById(R.id.textViewRpmValue);
        textViewRuntimeValue = (TextView) findViewById(R.id.textViewRuntimeValue);
        textViewEngineLoadValue = (TextView) findViewById(R.id.textViewEngineLoadValue);

        chronometer = (Chronometer) findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
        {
            @Override
            public void onChronometerTick(Chronometer chronometer)
            {
                long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                elapsedMillis /= 1000;
                progressBarStandTime.setMax((int) elapsedMillis);
                progressBarStandTime.setProgress((int) standTime/1000);
            }
        });

        drawerList = (ListView)findViewById(R.id.drawerList);
        registerForContextMenu(drawerList);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        title = getTitle().toString();
        addDrawerItems();

        isStanding     = false;
        startStandTime = 0;
        standTime      = 0;
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
                TripRow row = dbHelper.selectTrip(i);
                tripStringArray.add(new String[] {row.getName(), String.valueOf(i), "(" + row.getDate() + ")"});
            }
        }else{
            // No trips found
            tripStringArray.add(noTripsString);
        }

        drawerAdapter = new ArrayAdapter<String[]>(this, R.layout.drawer_row, tripStringArray){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // 1. Create inflater
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                // 2. Get view from inflater
                View view = inflater.inflate(R.layout.drawer_row, parent, false);

                // 3. Get the textViews from the view
                TextView tripName = (TextView) view.findViewById(R.id.drawerRowName);
                TextView tripId   = (TextView) view.findViewById(R.id.drawerRowId);
                TextView tripDate = (TextView) view.findViewById(R.id.drawerRowDate);

                // 4. Set the text for textViews
                String[] row = tripStringArray.get(position);
                tripName.setText(row[0]);
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
                TextView tripName = (TextView) group.findViewById(R.id.drawerRowName);
                TextView tripId   = (TextView) group.findViewById(R.id.drawerRowId);

                //do not show the TripMessage, if the user clicks the "no trips found" text
                if (!tripName.getText().equals(getString(R.string.noTripsFound)))
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
        if (tripStringArray.isEmpty()){
            tripStringArray.add(noTripsString);
        }else{
            if (tripStringArray.contains(noTripsString)) {
                tripStringArray.remove(noTripsString);
            }
        }
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
     * create Context Menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        ViewGroup group = (ViewGroup) view;
        TextView tripName = (TextView) group.findViewById(R.id.drawerRowName);

        //do not show the TripMenu, if the user holds "No trips found" text
        if (!tripName.getText().equals(getString(R.string.noTripsFound))){
            super.onCreateContextMenu(menu, view, menuInfo);

            if (view.getId() == R.id.drawerList) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.trip_menu, menu);
            }
        }
    }

    /**
     * implements the behavior of the trip context menu
     */
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        int tripId = Integer.parseInt(drawerAdapter.getItem(pos)[1]);
        String tripName = (drawerAdapter.getItem(pos)[0]);

        switch (item.getItemId()){
            case R.id.tripShow:
                showTripMessage(tripId);
                return true;
            case R.id.tripRename:
                showRenameTripDialog(tripId, tripName);
                return true;
            case R.id.tripDelete:
                showDeleteTripDialog(tripId, tripName);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * shows the dialog to enter a new tripName
     */
    private void showRenameTripDialog(final int id, String tripName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.enterTripNameTitle);

        //Input EditText
        final EditText input = new EditText(this);
        input.setText(tripName);
        //Highlight all text
        input.setSelectAllOnFocus(true);
        //Text input. Capitalizes the first letter after a ". " and at the beginning of the text
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //Max Length is 15 chars
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});

        //put EditText into Dialog
        builder.setView(input);
        //OK Button: renaming the Trip
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                renameTrip(id, input.getText().toString());
            }
        });
        //Cancel Button: close the dialog and do nothing
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        //Open keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
        //Listener, if the User clicks on ENTER on the keyboard, it will also rename the trip
        // and closes the dialog
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER){
                    renameTrip(id, input.getText().toString());
                    dialog.cancel();
                }
                return false;
            }
        });
    }

    /**
     * updates the trip in the database and drawer
     */
    private boolean renameTrip(int id, String newTripName) {

        //Just update the database and drawer, if the user enters NOT an empty String
        if(newTripName != null
            && !newTripName.isEmpty())
        {
            String oldTripName;
            TripRow tripRow = dbHelper.selectTrip(id);
            if(tripRow != null) oldTripName = tripRow.getName();
            else
            {
//              there is no trip with this id
                return false;
            }

            //Just update the database and drawer, if the user enters a NEW name
            if(!newTripName.equals(oldTripName))
            {
                dbHelper.updateTripName(id, newTripName);

                for (String[] row : tripStringArray)
                {
                    if (row[1].equals(String.valueOf(id)))
                    {
                        tripRow = dbHelper.selectTrip(id);
                        if(tripRow != null) oldTripName = tripRow.getName();
                        row[0] = oldTripName;
                    }
                }

                refreshDrawer();

                return true;
            }
        }
        return false;
    }

    /**
     * show the dialog, if you really want to delete this trip
     */
    private void showDeleteTripDialog(final int id, final String tripName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(tripName);
        builder.setMessage(R.string.tripDeleteQuestion);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTrip(id, tripName);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }


    /**
     * deletes the trip from the drawer and database
     */
    private void deleteTrip(int id, final String tripName) {

        String [] rowToDelete = new String[3];

        for (String[] row : tripStringArray){
            if (Integer.parseInt(row[1]) == id){
                rowToDelete = row;
            }
        }
        //save the position in the list, in case of an undo, to reset it in this position
        indexOfRowToDelete = tripStringArray.indexOf(rowToDelete);

        //select trip to delete
        deletedTripRow = dbHelper.selectTrip(id);

        //delete trip from database, if successful, make snackBar confirmation
        Resources res = getResources();
        String formattedText = String.format(res.getString(R.string.tripDeleteConfirmation), tripName);

        if (dbHelper.deleteTrip(id)){
            //delete trip from the drawer
            tripStringArray.remove(rowToDelete);

            Snackbar.make(findViewById(R.id.drawer_layout), formattedText, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            undoDeleteTrip(tripName);
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(this, R.color.lightBlue))
                    .show();
        }else{
            Toast.makeText(getApplicationContext(), R.string.DeleteError, Toast.LENGTH_LONG).show();
        }


        refreshDrawer();
    }

    private void undoDeleteTrip(String tripName) {
        //insert the deleted row back in the database
        if (dbHelper.insertTripData(deletedTripRow)) {
            //add Trip to the drawerList at the same position, where it was deleted
            tripStringArray.add(indexOfRowToDelete, new String[] {deletedTripRow.getName(), String.valueOf(deletedTripRow.getTripId()), "(" + deletedTripRow.getDate() + ")"});

            //make snackBar confirmation
            Resources res = getResources();
            String formattedText = String.format(res.getString(R.string.tripDeleteUndoConfirmation), tripName);
            Snackbar.make(findViewById(R.id.drawer_layout), formattedText, Snackbar.LENGTH_LONG).show();
        }
        else
        {
            Resources res = getResources();
            String formattedText = String.format(res.getString(R.string.tripDeleteUndoError), tripName);
            Snackbar.make(findViewById(R.id.drawer_layout), formattedText, Snackbar.LENGTH_LONG).show();
        }
        refreshDrawer();
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

            final CharSequence[] deviceNames = new String[deviceArray.size()];

            for(int i=0; i<deviceArray.size(); i++)
                deviceNames[i] = deviceArray.get(i).getName();

            if(devicesSet.size() > 0)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.adapterSelectionDialogCaption);
                builder.setItems(deviceNames, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, final int which)
                    {
                        Animation an = new RotateAnimation(0, 359,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                                0.5f);

                        an.setDuration(800);
                        an.setRepeatCount(Animation.INFINITE);
                        an.setFillAfter(true);

                        buttonStartStop.startAnimation(an);

                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(createConnection(deviceArray.get(which)))
                                {
                                    new Handler(Looper.getMainLooper()).post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            isRunning = true;
                                            startLiveData();

                                            chronometer.setBase(SystemClock.elapsedRealtime());
                                            chronometer.start();
                                            buttonStartStop.setBackgroundResource(R.drawable.stop_68);
                                        }
                                    });
                                }
                                new Handler(Looper.getMainLooper()).post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        buttonStartStop.setAnimation(null);
                                    }
                                });

                            }
                        }).start();

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
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText( getApplicationContext()
                            , "Connection failed, try again."
                            , Toast.LENGTH_SHORT
                    ).show();
                }
            });

            return false;
        }
        return true;
    }

    private void startLiveData()
    {
        adapterAgent = new AdapterAgent(dbHelper, socket, locationFinder);

        currentTripId = dbHelper.getLatestTripId() + 1;

        timerAdapterAgent = new Timer();
        timerAdapterAgent.schedule(new TaskAdapterAgent(), 0);

        timerPresenter = new Timer();
        timerPresenter.schedule(new TaskPresenter(), 0);
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
            updateProgressBar(progressBarThrottlePosition   , dataRow.getThrottlePosition());
            updateTextView(textViewRuntimeValue             , dataRow.getRunTimeString());

            int speed = dataRow.getSpeed();

            //stops or stands still
            if(speed == 0 && !isStanding)
            {
                startStandTime = SystemClock.elapsedRealtime();
                isStanding = true;
            }

            //starts driving or is driving
            if(speed > 0 && isStanding)
            {
                standTime += SystemClock.elapsedRealtime() - startStandTime;
                isStanding = false;
            }
        }

//      live data
        for(Pair<String, String> pair : new ArrayList<>(adapterAgent.getLiveData()))
                refreshTextView(pair.first, pair.second);
    }

    private void refreshTextView(final String name, final String value)
    {
        TextView textView = mapLiveTextViews.get(name);

        if(textView != null) updateTextView(textView, value);
        else
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                public void run()
                {
                    TableRow tableRow       = new TableRow(getApplicationContext());
                    TextView textViewName   = new TextView(getApplicationContext());
                    TextView textViewValue  = new TextView(getApplicationContext());

                    textViewName.setTextColor(Color.WHITE);
                    textViewName.setText(name);
                    textViewValue.setTextColor(Color.WHITE);
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

    private void updateProgressBar(final ProgressBar bar, final double value)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            public void run()
            {
                bar.setProgress((int) value);
            }
        });
    }

    private void updateImageView(final ImageView image, final float fromRotation, final float toRotation)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            public void run()
            {
                //because final declaration
                float newToRotation = toRotation;
                float newFromRotation = fromRotation;

    /***********************************************************************************************
    *        Old Version                                                                           *
    ************************************************************************************************/
//                //if the rotation starts from left-top to right-top (example: from 350° to 15°)
//                //it would move counterclockwise, but should go clockwise.
//                //So the new location would be 375° (example: from 350° to 375°)
//                //(similar from other site)
//                if ((fromRotation > 270 && fromRotation <= 360) && (toRotation >= 0 && toRotation < 90)){
//                    // from (West-Nord) to (Nord-East)
//                    newToRotation = toRotation + 360;
//                }else if ((fromRotation >= 0 && fromRotation < 90) && (toRotation > 270 && toRotation<= 360)){
//                    // from (Nord-East) to (West-Nord)
//                    newFromRotation = fromRotation + 360;
//                }

                if ((fromRotation - toRotation) > 180){
                    newToRotation = toRotation + 360;
                }
                if ((fromRotation - toRotation) < -180){
                    newFromRotation = fromRotation + 360;
                }

                Animation animation = new RotateAnimation(-newFromRotation, -newToRotation,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                animation.setDuration(COMPASS_DELAY);
                animation.setRepeatCount(0);
                animation.setFillAfter(true);

                image.startAnimation(animation);
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
                                          , INPUT_DATA_DELAY);
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
            if(isRunning) timerPresenter.schedule(new TaskPresenter(), PRESENTER_DELAY);
        }
    }

    private void endTrip()
    {
        makeNewTrip();

        endStuff();
    }

    private void makeNewTrip(){
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(R.string.enterTripNameTitle);

                //Input EditText
                final EditText input = new EditText(MainActivity.this);

                //Highlight all text
                input.setSelectAllOnFocus(true);
                //Text input. Capitalizes the first letter after a ". " and at the beginning of the text
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                //Max Length is 15 chars
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});

                //put EditText into Dialog
                builder.setView(input);
                //OK Button: naming the Trip
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        TripCalculator.calculate(dbHelper, currentTripId, (int) standTime/1000, drivingTime, input.getText().toString(), getApplicationContext());
                        showNewTrip();
                    }
                });

                final AlertDialog dialog = builder.create();
                //Open keyboard
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                //Dialog can be dismissed by clicking outside of it
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                //Listener, if the User clicks on ENTER on the keyboard, it will also name the trip
                // and closes the dialog
                input.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == event.KEYCODE_ENTER){
                            TripCalculator.calculate(dbHelper, currentTripId, (int) standTime/1000, drivingTime, input.getText().toString(), getApplicationContext());
                            showNewTrip();
                            dialog.dismiss();
                        }
                        return false;
                    }
                });

                //if User cancels the dialog, then the tripName is default
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        TripCalculator.calculate(dbHelper, currentTripId, (int) standTime/1000, drivingTime, null, getApplicationContext());
                        showNewTrip();
                    }
                });
            }
        });
    }

    private void showNewTrip()
    {
        TripRow tripRow = dbHelper.selectTrip(currentTripId);
        //add tripName, id, date to the top of the List and update the listAdapter
        tripStringArray.add(0, new String[]{tripRow.getName(), String.valueOf(currentTripId), "(" + tripRow.getDate() + ")"});

        refreshDrawer();

        showTripMessage(currentTripId);
    }

    private void endStuff()
    {
        //if trip is still running then save trip Data, for example before the app is going be destroyed
        if (isRunning)
            TripCalculator.calculate(dbHelper, currentTripId, (int) standTime/1000, drivingTime, null, getApplicationContext());

        //stop gps stuff
        if(locationFinder != null) locationFinder.stopGps();

        try
        {
            if(socket != null) socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

//      reset gui
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                updateTextView(textViewEngineLoadValue          , "0");
                updateTextView(textViewRpmValue                 , "0");
                updateTextView(textViewSpeedValue               , "0");
                updateProgressBar(progressBarThrottlePosition   ,  0 );
                updateProgressBar(progressBarStandTime          ,  0 );
                updateTextView(textViewRuntimeValue             , "0");

                drivingTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                drivingTime /= 1000;
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());

                tableLayoutData.removeAllViews();
                mapLiveTextViews.clear();

                buttonStartStop.setBackgroundResource(R.drawable.start_68);
            }
        });
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
                    InfoDialog.show(MainActivity.this
                            , String.format(getResources().getString(R.string.tripAlertCaption), tripRow.getName())
                            , "Distanz: " + tripRow.getDistance() + "\n"
                                    + "Maximalgeschwindigkeit: " + tripRow.getMaxSpeed() + "\n"
                                    + "Durchschnittsgeschwindigkeit: " + tripRow.getAvgSpeed() + "\n"
                                    + "Fahrzeit: " + tripRow.getRunTime() / 60 + " Minuten " + tripRow.getRunTime() % 60 + " Sekunden" + "\n"
                                    + "Stehzeit: " + tripRow.getStandTime() / 60 + " Minuten " + tripRow.getStandTime() % 60 + " Sekunden"
                            );
                }
            }
        });
    }
}