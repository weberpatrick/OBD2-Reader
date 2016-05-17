package obd2.dhbw.de.obd2_reader.dialog;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by Ricardo on 17.05.2016.
 */
public class InfoDialog
{
    public static void show( Context context
                    , String title
                    , String message
                    )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", null);

        builder.create().show();
    }
}
