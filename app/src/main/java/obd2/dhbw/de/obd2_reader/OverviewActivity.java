package obd2.dhbw.de.obd2_reader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class OverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        TableLayout tl = (TableLayout)findViewById(R.id.tableLayoutData);

        for (int i = 0; i < 100; i++) {
            TableRow row = new TableRow(this);
            TextView textView = new TextView(this);
            textView.setText("Nummer:"+i);
            row.addView(textView);
            tl.addView(row);
        }

    }
}
