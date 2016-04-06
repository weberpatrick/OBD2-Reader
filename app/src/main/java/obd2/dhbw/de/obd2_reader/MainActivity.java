package obd2.dhbw.de.obd2_reader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import obd2.dhbw.de.obd2_reader.connection.BluetoothConnector;
import obd2.dhbw.de.obd2_reader.storage.DbHelper;

public class MainActivity
        extends AppCompatActivity
        implements AdapterView.OnItemClickListener
{
//	***************************************************************************
//	DECLARATION OF VARIABLES
//	***************************************************************************

    private String LOG_TAG = MainActivity.class.getName();

    private ArrayAdapter<String> listAdapter;
    private ListView listView;

    public SQLiteDatabase db;

    ArrayList<BluetoothDevice> pairedDevices;

    private BluetoothSocket socket;
    private BluetoothAdapter btAdapter;

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    private void init()
    {
        listView = (ListView) findViewById(R.id.listViewPairedDevices);
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<>( this
                                        , android.R.layout.simple_list_item_1
                                        ,0
                                        );
        listView.setAdapter(listAdapter);
        pairedDevices = new ArrayList<BluetoothDevice>();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void fillListView()
    {
        Set<BluetoothDevice> devicesArray = btAdapter.getBondedDevices();

        if(devicesArray.size() > 0)
        {
            for(BluetoothDevice device:devicesArray)
            {
                pairedDevices.add(device);
                listAdapter.add(device.getName());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No paired device detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
        fillListView();

        //database();
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

    @Override
    public void onItemClick( AdapterView<?> parent
                           , View view
                           , int position
                           , long id
                           )
    {
        createConnection(pairedDevices.get(position));

        //show overview activity
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
            db.insert(DbHelper.DICTIONARY_TABLE_NAME, null, values);
        }

//      declare cursor to read data
        Cursor cursor = db.query(DbHelper.DICTIONARY_TABLE_NAME
                , new String[]{DbHelper.COLUMN_NAME} //columns
                , null //DbHelper.COLUMN_ID +"=1" //where clause
                , null
                , null
                , null
                , null
        );

        Log.d(LOG_TAG, "cursor length: " + cursor.getCount());

//      fetch cursor
        for(int i = 0; i < cursor.getCount(); i++)
        {
            if(cursor.moveToPosition(i)) Log.d(LOG_TAG, cursor.getString(0));
        }
    }

    private void createConnection(BluetoothDevice device)
    {
        if(device == null) Log.e(LOG_TAG, "The device should not be null here!");
        else               socket = BluetoothConnector.connect(device);

        if(socket == null)
        {
            Toast.makeText( getApplicationContext()
                          , "No paired device detected"
                          , Toast.LENGTH_SHORT
                          ).show();
        }
    }


}
