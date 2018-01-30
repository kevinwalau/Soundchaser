package cs48.soundchaser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ViewData extends AppCompatActivity{

    LocationsDB db;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_data);
        readDataBase();
        context = getBaseContext();
    }


    private void readDataBase() {
        db = new LocationsDB(this);
        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts..");
        List<LocationList> contacts = db.getAllLocations();
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.buttonsLayout);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y/6;

        for (LocationList cn : contacts) {
            Button addButton = new Button(this);
            int duration = Integer.parseInt(cn.getDuration());
            int min = duration/60;
            int sec = duration%60;
            String dur = Integer.toString(min) + ":";
            if(sec < 10)
            {
                dur+="0";
            }
            dur+=Integer.toString(sec);
            addButton.setText(cn.getTime() + "\ndistance: " + cn.getDistance() + "\nduration: " + dur);
            addButton.setId(cn.getId());
            addButton.setClickable(true);
            addButton.setWidth(width);
            addButton.setHeight(height);
            addButton.setTextSize(20);
            addButton.setOnClickListener(btnclick);
            mainLayout.addView(addButton);

            //debugg
            String log = "Id: " + cn.getId() + " ,time: " + cn.getTime() + " ,list: " + cn.getList()+ " ,distance: " + cn.getDistance() + " duration: " + cn.getDuration();
            // Writing Contacts to log
            Log.d("Name: ", log);
        }
    }

    Button.OnClickListener btnclick = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            Button button = (Button)v;
            int id = v.getId();
            LocationList list = db.getLocations(id);
            Intent i = new Intent(context, displayPastRoutes.class);
            i.putExtra("list",list.getList());
            startActivity(i);
            finish();
            //Toast.makeText(getApplicationContext(), button.getText().toString(),Toast.LENGTH_SHORT).show();
        }

    };

    public void onBackPressed() {

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();

    }


}
