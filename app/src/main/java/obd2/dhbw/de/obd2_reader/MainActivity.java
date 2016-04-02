package obd2.dhbw.de.obd2_reader;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity
       extends AppCompatActivity
{
    public SQLiteDatabase db;

    private String LOG_TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        Cursor cursor = db.query( DbHelper.DICTIONARY_TABLE_NAME
                                , new String[] {DbHelper.COLUMN_NAME} //columns
                                , null //DbHelper.COLUMN_ID +"=1" //where clause
                                , null
                                , null
                                , null
                                , null
                                );

        Log.d(LOG_TAG, "cursor length: " +  cursor.getCount());

//      fetch cursor
        for(int i = 0; i < cursor.getCount(); i++)
        {
            if(cursor.moveToPosition(i)) Log.d(LOG_TAG, cursor.getString(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
}
