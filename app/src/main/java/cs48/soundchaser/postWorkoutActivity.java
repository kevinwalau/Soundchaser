package cs48.soundchaser;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Krassi on 2/20/2016.
 */
public class postWorkoutActivity {
    String duration;
    GoogleMap mMap;
    String totalDistance;
    String time;
    LocationsDB db;

    public postWorkoutActivity(long d, GoogleMap m, double tD, LocationsDB db)
    {
        duration = Long.toString(d);
        mMap = m;
        totalDistance = Double.toString(tD);
        this.db = db;
        displayRun();
        //get current time.
        DateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy 'at' hh:mm a");
        dateFormatter.setLenient(false);
        Date today = new Date();
        time = dateFormatter.format(today);
        initDataBase();
    }

    private void initDataBase()
    {

        /**
         * CRUD Operations
         * */
        // Inserting Contacts
        Log.d("dataBase: ", "Inserting ..");
        int count = db.getLocationCount();
        String list = routeToString();
        db.addLocations(new LocationList(count, time, list, totalDistance, duration));

    }

    private String routeToString()
    {
        String route = "";
        for(LatLng x : Globals.getListOfLocations())
        {
            route+=Double.toString(x.latitude);
            route+="_";
            route+=Double.toString(x.longitude);
            route+="_";
        }
        return route;
    }

    private void displayRun()
    {
        drawPath();
        fixZoom(Globals.getListOfLocations());

    }

    private void drawPath()
    {
        if(mMap == null)
        {
            return;
        }
        mMap.addPolyline(new PolylineOptions()
                        .addAll(Globals.getListOfLocations())
                        .width(12)
                        .color(Color.parseColor("#05b1fb"))//Google maps blue color
                        .geodesic(true)
        );

    }

    private void fixZoom(List<LatLng> list)
    {
        LatLngBounds.Builder bc = new LatLngBounds.Builder();

        for (LatLng item : list) {
            bc.include(item);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
    }


}
