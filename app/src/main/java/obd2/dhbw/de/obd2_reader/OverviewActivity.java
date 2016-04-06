package obd2.dhbw.de.obd2_reader;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class OverviewActivity
       extends AppCompatActivity
{
//	***************************************************************************
//	DECLARATION OF VARIABLES
//	***************************************************************************

    private final String LOG_TAG = OverviewActivity.class.getName();

    private BluetoothSocket socket;

//	***************************************************************************
//	METHOD AREA
//	***************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
    }
}
