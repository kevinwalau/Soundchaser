package cs48.soundchaser;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Krassi on 1/23/2016.
 */
public class Help extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);
        makeText();
    }

    private void makeText()
    {
        String text = readFromFile();
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.textLayout);
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(30);
        t.setTextColor(Color.BLACK);
        mainLayout.addView(t);

    }

    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = getBaseContext().getResources().openRawResource(R.raw.help_text);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        //Log.e("test",ret);
        return ret;
    }
}
