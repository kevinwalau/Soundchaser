package cs48.soundchaser;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class displayPastRoutes extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    List<LatLng> list;
    boolean parsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_past_routes);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        list = new ArrayList<LatLng>();
        Intent myIntent = getIntent(); // gets the previously created intent
        String listOfLocs = myIntent.getStringExtra("list"); // will return "list" value
        if(listOfLocs!= "" || listOfLocs != null)
            parseLocationString(listOfLocs,list);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.clear();
        if(parsed) {
            placeMarker(list.get(0), Icon.START_FLAG);
            placeMarker(list.get(list.size() - 1), Icon.FINISH_FLAG);
            drawPath(list);
            fixZoom(list);
        }

    }

    public void placeMarker(LatLng latLng, Icon i)
    {
        int deviceW = getResources().getDisplayMetrics().widthPixels;
        int deviceH = getResources().getDisplayMetrics().heightPixels;
        int scale = Math.min(deviceH,deviceW);
        int newWidth = scale/10;
        int newHeight = scale/10;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        Bitmap b;

        switch(i)
        {
            case START_FLAG:
                b = resizeBitmap(R.drawable.start_flag, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                markerOptions.title("Start Position");
                mMap.addMarker(markerOptions);
                return;

            case FINISH_FLAG:
                b = resizeBitmap(R.drawable.finish_flag, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                markerOptions.title("End Position");
                mMap.addMarker(markerOptions);
                return;
        }
    }

    private Bitmap resizeBitmap(int id, int w, int h)
    {
        Bitmap bitMap = BitmapFactory.decodeResource(getResources(), id, null);
        Bitmap resized = Bitmap.createScaledBitmap(bitMap, w, h, true);
        return resized;
    }

    private void drawPath(List<LatLng> list)
    {
        if(list.size() < 1)
        {
            return;
        }

        mMap.addPolyline(new PolylineOptions()
                        .addAll(list)
                        .width(12)
                        .color(Color.parseColor("#05b1fb"))//Google maps blue color
                        .geodesic(true)
        );

    }

    private  void parseLocationString(String s, List<LatLng> locs)
    {
        String[] parts = s.split("_");
        for(int i =0; i < parts.length; i+=2)
        {
            double lat = Double.parseDouble(parts[i]);
            double lon = Double.parseDouble((parts[i+1]));
            LatLng latLon = new LatLng(lat,lon);
            locs.add(latLon);
        }
        parsed = true;
    }

    private void fixZoom(List<LatLng> list)
    {
        LatLngBounds.Builder bc = new LatLngBounds.Builder();

        for (LatLng item : list) {
            bc.include(item);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
    }

    public void onBackPressed() {

        Intent intent = new Intent(this,ViewData.class);
        startActivity(intent);
        finish();

    }
}
