package cs48.soundchaser;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.content.IntentSender;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.*;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OurGoogleMap extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final String TAG = OurGoogleMap.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    //private static final LatLng STORKE_TOWER = new LatLng(34.4126047, -119.8484183);

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private float zoomLevel;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Marker mCurrLocation;
    Marker startLocation;
    Marker finishLocation;
    protected int mDpi = 0;
    NotificationManager mNotifyMgr;
    String address;
    boolean randomPathGenerationBegun = false;
    CircleOptions options;
    Circle preLimCircle;
    private ArrayList<LatLng> points; //added
    Location previusLocation;
    long timeWhenStopped = 0;
    Chronometer Mchronometer;
    PowerManager mgr;
    PowerManager.WakeLock wakeLock;
    boolean firstRun = true;
    boolean ended = false;
    PendingIntent mRequestLocationUpdatesPendingIntent;
    boolean foreground = true;
    boolean connected = false;
    RandomPathGenerator r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("runnin", "onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        setUpMapIfNeeded();
        mDpi = getResources().getDisplayMetrics().densityDpi;
        points = new ArrayList<LatLng>(); //added

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(Globals.getaType().getSmallestDisp())   //5 meter
                .setInterval(Globals.getaType().getInterval())        // 5 seconds, in milliseconds
                .setFastestInterval(Globals.getaType().getFastestInterval()); // 3 second, in milliseconds
        try {
            mMap.setMyLocationEnabled(true);
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }
        previusLocation = null;
        Mchronometer = (Chronometer) findViewById(R.id.timer);
        mgr = (PowerManager)getBaseContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("LocationChanged"));
        setUpNotification();
        Log.i("runnin", "onCreate finished");

    }


    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Double lat = intent.getDoubleExtra("lat", 0);
            Double lon = intent.getDoubleExtra("lon", 0);
            Location loc = new Location("");
            loc.setLatitude(lat);
            loc.setLongitude(lon);
            if(foreground)
            {
                onLocationChanged(loc);
            }
            else
            {
                if(validChange(loc)) {
                    double currentLatitude = loc.getLatitude();
                    double currentLongitude = loc.getLongitude();
                    LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                    if(Globals.getStartLocation() == null)
                    {
                        Globals.setStartLocation(latLng);
                    }
                    else
                    {
                        Globals.setCurrentLocation(latLng);
                    }
                }
            }

            Log.i("locationtesting", "location recieved: " + " lat: " + loc.getLatitude() + " lon: " + loc.getLongitude());
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("runnin", "onResume start");
        if(ended)
        {
            setUpMapIfNeeded();
            return;
        }
        setUpMapIfNeeded();
        if(!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        foreground = true;
        if(foreground && connected)
            drawPath(Globals.getListOfLocations());

        //wakeLock.acquire();
        Log.i("runnin", "onResume finish");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("runnin", "onPause start");
        if(ended)
        {
            return;
        }

        foreground = false;
        /*
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mRequestLocationUpdatesPendingIntent);
            mGoogleApiClient.disconnect();
        }
        */
        //wakeLock.release();
        Log.i("runnin", "onPause finsished");
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        Log.i("runnin", "setUpMapIFneeded start");
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
        Log.i("runnin", "setUpMapIFneeded finish");
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        Log.i("runnin", "setUpMap start");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("runnin", "onConnected start");
        try {
            // create the Intent to use WebViewActivity to handle results
            Intent mRequestLocationUpdatesIntent = new Intent(this, LocationUpdateService.class);

            // create a PendingIntent
            mRequestLocationUpdatesPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                    mRequestLocationUpdatesIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            // request location updates
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest,
                    mRequestLocationUpdatesPendingIntent);
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(location == null)
            {
                //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            previusLocation = location;
            if(firstRun) {
                handleStart(location);
                firstRun = false;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        Log.i("runnin", "onConnected finsihed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("runnin", "onConnectionSuspended start");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    /*
     * Google Play services can resolve some errors it detects.
     * If the error has a resolution, try sending an Intent to
     * start a Google Play services activity that can resolve
     * error.
     */
        Log.i("runnin", "onConnectionFailed start");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
        /*
         * If no resolution is available, display a dialog to the
         * user with the error.
         */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
        Log.i("runnin", "onConnectionFailed finsih");
    }

    private boolean validChange(Location location)
    {
        if(previusLocation == null)
        {
            return true;
        }
        boolean val = false;
        double disp = location.distanceTo(previusLocation);
        if(disp < Globals.getaType().getValidDisp())
        {
            val = true;
        }
        previusLocation = location;
        return val;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("runnin", "********onLocationChanged start");
        if(validChange(location)) {
            handleNewLocation(location);
        }
        Log.i("runnin", "********onLocationChanged finish");

    }

    public void handleNewLocation(Location location) {
        Log.i("runnin", "handleNewLocation start");
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        if(Globals.getStartLocation() == null)
        {
            Globals.setStartLocation(latLng);
        }
        else
        {
            Globals.setCurrentLocation(latLng);
        }
        if (mCurrLocation != null) {
            mCurrLocation.remove();
        }
        placeMarker(latLng, Globals.getaType().getIcon());
        points.add(latLng);
        drawPath();
        points.remove(0);
        Log.i("runnin", "handleNewLocation finish");
    }

    private void drawCircle( LatLng location) {
        if (mMap == null || !Globals.getDrawRadius()) {
            return;
        }
        options = new CircleOptions();
        options.center(location);
        //Radius in meters
        double radius = Globals.getMaximumRadius() * 1000;
        options.radius(radius);
        options.strokeWidth(10);
        preLimCircle = mMap.addCircle(options);
        Globals.setDrawRadius(false);
        radius = radius + radius/2;
        double scale = radius / 500;
        zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));

        /* JUST A TEST FOR RANDOM POINT, RADIUS MUST BE LESS THAN DISTTORUN, CLICK CUSTOM DEST
        for (int i = 0; i < 100; i++) {
            RandomPoint pt = new RandomPoint(Globals.getCurrentLocation(), Globals.getMaximumRadius());
            mMap.addMarker(new MarkerOptions()
                    .position(pt.getCoordinates())
                    .title("Hello world"));
        }
         TEST ENDS HERE */
    }

    private void reScaleCircle( double radius) {
        if (mMap == null) {
            return;
        }
        if(preLimCircle != null)
        {
            preLimCircle.remove();
        }
        options.radius(radius);
        preLimCircle =mMap.addCircle(options);
        Globals.setMaximumRadius(radius / 1000);
        radius = radius + radius/2;
        double scale = radius / 500;
        zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));

    }

    private void drawPath()
    {
        mMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .width(12)
                        .color(Color.parseColor("#05b1fb"))//Google maps blue color
                        .geodesic(true)
        );

    }

    private void drawPath(List<LatLng> l)
    {
        if(l.size() < 1)
        {
            return;
        }
        mMap.clear();
        mMap.addCircle(options);
        placeMarker(l.get(l.size() - 1), Globals.getaType().getIcon());
        placeMarker(startLocation.getPosition(), Icon.START_FLAG);
        placeMarker(finishLocation.getPosition(), Icon.FINISH_FLAG);
        mMap.addPolyline(new PolylineOptions()
                        .addAll(l)
                        .width(12)
                        .color(Color.parseColor("#05b1fb"))//Google maps blue color
                        .geodesic(true)
        );
        if(l.get(l.size()-1) != null)
        {
            points.remove(0);
            points.add(l.get(l.size() - 1));
            previusLocation.setLatitude(l.get(l.size() - 1).latitude);
            previusLocation.setLatitude(l.get(l.size()-1).longitude);
        }
        drawRandomPath();

    }

    private void drawRandomPath()
    {
        for(PolylineOptions p :r.getGeneratedPath())
            mMap.addPolyline(p);
    }

    public void handleStart(Location location) {
        Log.i("runnin", "handleStart start");
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        if (Globals.getStartLocation() == null) {
            Globals.setStartLocation(latLng);
        } else {
            Globals.setCurrentLocation(latLng);
        }

        placeMarker(latLng, Icon.START_FLAG);
        zoomLevel = 15; //This goes up to 21
        drawCircle(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        points.add(latLng);
        handleNewLocation(location);
        if (Globals.getCustomDestination())
            chooseDestination();
        else
            generateRandomPath();
        connected = true;
        Log.i("runnin", "handleStart finsih");
    }

    private float getDistanceBetweenCurrentAndFinish()
    {
        if(mCurrLocation == null || finishLocation == null)
        {
            return (float)(Globals.getMaximumRadius()*1000);
        }
        float[] newR = new float[1];
        Location.distanceBetween(mCurrLocation.getPosition().latitude, mCurrLocation.getPosition().longitude, finishLocation.getPosition().latitude, finishLocation.getPosition().longitude, newR);
        //Toast.makeText(getBaseContext(), Float.toString(newR[0]), Toast.LENGTH_SHORT).show();
        return newR[0];
    }

    public void generateRandomPath(View v)
    {
        generateRandomPath();
    }

    public void generateRandomPath() {
        if (!Globals.getCustomDestination()) {
            r = new RandomPathGenerator(mCurrLocation.getPosition(), this, mMap);
            r.generate(Globals.getStartLocation(), null);
            randomPathGenerationBegun = true;
            findViewById(R.id.startWorkoutButton).setVisibility(View.VISIBLE);
            findViewById(R.id.regenerateButton).setVisibility(View.VISIBLE);
            placeMarker(startLocation.getPosition(),Icon.FINISH_FLAG);
        }
        else {
            if(finishLocation != null) {
                float newRadius = getDistanceBetweenCurrentAndFinish();
                reScaleCircle(newRadius+100);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrLocation.getPosition(), zoomLevel));
                r = new RandomPathGenerator(mCurrLocation.getPosition(), this, mMap);
                r.generate(Globals.getStartLocation(), finishLocation.getPosition());
                randomPathGenerationBegun = true;
                makeGone();
                findViewById(R.id.startWorkoutButton).setVisibility(View.VISIBLE);
            }
            else
            {
                Toast.makeText(getBaseContext(), "You must chose a destination", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void placeMarker(LatLng latLng, Icon i)
    {
        int deviceW = getResources().getDisplayMetrics().widthPixels;
        int deviceH = getResources().getDisplayMetrics().heightPixels;
        int scale = Math.min(deviceH,deviceW);
        Log.i("size", "width = " + deviceW + " and height = " + deviceH);
        int newWidth = scale/10;
        int newHeight = scale/10;
        Log.i("size", "width = " + newWidth + " and height = " + newHeight);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Position");
        Bitmap b;

        switch(i)
        {
            case START_FLAG:
                b = resizeBitmap(R.drawable.start_flag, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                markerOptions.title("Start Position");
                startLocation = mMap.addMarker(markerOptions);
                return;
            case BIKER:
                //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.biker));
                b = resizeBitmap(R.drawable.biker, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                break;
            case HIKER:
                b = resizeBitmap(R.drawable.hiker, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                break;
            case RUN_MAN:
                b = resizeBitmap(R.drawable.run_man, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                break;
            case WALK_MAN:
                b = resizeBitmap(R.drawable.walk_man, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                break;
            case FINISH_FLAG:
                if (finishLocation != null) {
                    finishLocation.remove();
                }
                b = resizeBitmap(R.drawable.finish_flag, newWidth, newHeight);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b));
                markerOptions.title("End Position");
                finishLocation = mMap.addMarker(markerOptions);
                return;
        }
        mCurrLocation = mMap.addMarker(markerOptions);
    }

    private Bitmap resizeBitmap(int id, int w, int h)
    {
        Bitmap bitMap = BitmapFactory.decodeResource(getResources(),id,null);
        Bitmap resized = Bitmap.createScaledBitmap(bitMap, w, h, true);
        return resized;
    }

    private  void chooseDestination()
    {
        makeVisible();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if(randomPathGenerationBegun)
                {
                    mMap.setOnMapClickListener(null);
                    return;
                }
                clearKeyboard();
                placeMarker(latLng, Icon.FINISH_FLAG);
            }
        });

    }

    public void handleAddress(View v)
    {
        if(validateAddress()) {
            clearKeyboard();
        }
    }

    private void makeGone()
    {
        findViewById(R.id.addressText).setVisibility(View.GONE);
        findViewById(R.id.addressButton).setVisibility(View.GONE);
        findViewById(R.id.confrimButton).setVisibility(View.GONE);
        clearKeyboard();
    }

    private  void makeVisible()
    {
        findViewById(R.id.addressText).setVisibility(View.VISIBLE);
        findViewById(R.id.addressButton).setVisibility(View.VISIBLE);
        findViewById(R.id.confrimButton).setVisibility(View.VISIBLE);
    }

    private void clearKeyboard()
    {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private boolean validateAddress()
    {
        address = ((EditText) findViewById(R.id.addressText)).getText().toString();
        if(address!=null && !address.equals("")){
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                double currLat = mCurrLocation.getPosition().latitude;
                double currLong = mCurrLocation.getPosition().longitude;
                // Getting a maximum of 1 Address that matches the input text
                addresses = geocoder.getFromLocationName(address, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            else
            {
                Address fAddress = addresses.get(0);
                //start Location
                Location startLoc = new Location("");
                startLoc.setLatitude(startLocation.getPosition().latitude);
                startLoc.setLongitude(startLocation.getPosition().longitude);
                //location of first postition
                LatLng latLng = new LatLng(fAddress.getLatitude(), fAddress.getLongitude());
                Location loc2 = new Location("");
                loc2.setLatitude(latLng.latitude);
                loc2.setLongitude(latLng.longitude);
                //smallest distance
                float minD = startLoc.distanceTo(loc2);

                for (int i = 0; i < addresses.size(); i++) {
                    Address tempAddress = (Address) addresses.get(i);
                    latLng = new LatLng(tempAddress.getLatitude(), tempAddress.getLongitude());

                    loc2 = new Location("");
                    loc2.setLatitude(latLng.latitude);
                    loc2.setLongitude(latLng.longitude);

                    float d = startLoc.distanceTo(loc2);

                    if(d < minD)
                    {
                        fAddress = tempAddress;
                    }

                }
                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(fAddress.getLatitude(), fAddress.getLongitude());
                placeMarker(latLng, Icon.FINISH_FLAG);
            }
            return true;
        }
        else {
            Toast.makeText(getBaseContext(), "Please enter a location first", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        returnRescourses();
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNeutralButton("Main Menu", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                returnRescourses();
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        }).create().show();
    }

    private void beginTimer()
    {
        findViewById(R.id.startWorkoutButton).setVisibility(View.GONE);
        findViewById(R.id.regenerateButton).setVisibility(View.GONE);
        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.endWorkoutButton).setVisibility(View.VISIBLE);
        startTimer();
    }

    private void startTimer()
    {
        Mchronometer.setBase(SystemClock.elapsedRealtime()
                + timeWhenStopped);
        Mchronometer.start();
    }

    private void pauseTimer()
    {
        timeWhenStopped = Mchronometer.getBase() - SystemClock.elapsedRealtime();
        Mchronometer.stop();
    }

    private void hideTimer()
    {
        pauseTimer();
        findViewById(R.id.timer).setVisibility(View.GONE);
        findViewById(R.id.endWorkoutButton).setVisibility(View.GONE);
    }

    private void resetTimer()
    {
        Mchronometer.setBase(SystemClock.elapsedRealtime());
        Mchronometer.stop();
        timeWhenStopped = 0;
    }

    private void confirmEnd()
    {
        new AlertDialog.Builder(this)
                .setTitle("Really Finish?")
                .setMessage("Are you sure you want to finish your workout?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        endWorkout();
                    }
                }).create().show();
    }

    private void endWorkout()
    {

        removeRepeats();
        ended = true;
        mMap.clear();
        placeMarker(Globals.getListOfLocations().get(0), Icon.START_FLAG);
        placeMarker(Globals.getListOfLocations().get(Globals.getListOfLocations().size() - 1), Icon.FINISH_FLAG);
        hideTimer();
        float totalD = totalDistance();
        findViewById(R.id.distanceRanText).setVisibility(View.VISIBLE);
        findViewById(R.id.timeRanText).setVisibility(View.VISIBLE);
        TextView dRT = ((TextView) findViewById(R.id.distanceRanText));
        dRT.setText("Total Distance: " + Float.toString(totalD));
        TextView tRT = ((TextView) findViewById(R.id.timeRanText));
        long timeInMillis = SystemClock.elapsedRealtime() - Mchronometer.getBase();
        long timeInSeconds = timeInMillis/1000;
        int minutes = (int) (timeInSeconds/60);
        int seconds = (int) (timeInSeconds%60);
        if(seconds > 9) {
            tRT.setText("Total Time: " + Integer.toString(minutes) + ":" + Integer.toString(seconds));
        }
        else
        {
            tRT.setText("Total Time: " + Integer.toString(minutes) + ":0" + Integer.toString(seconds));
        }
        LocationsDB db = new LocationsDB(this);
        new postWorkoutActivity(timeInSeconds, mMap, totalD, db);
        returnRescourses();

    }

    public void endWorkout(View v)
    {
        confirmEnd();
    }

    public void startWorkout(View v) {
        placeMarker(Globals.getNonCustomDest(),Icon.FINISH_FLAG);
        beginTimer();
    }

    public void regeneratePath(View v) {
        mMap.clear();
        generateRandomPath();
        drawPath(Globals.getListOfLocations());
    }

    private float distanceBetweenLatLngs(LatLng p1, LatLng p2)
    {
        //location of first postition
        Location loc1 = new Location("");
        loc1.setLatitude(p1.latitude);
        loc1.setLongitude(p1.longitude);
        //location of second postition
        Location loc2 = new Location("");
        loc2.setLatitude(p2.latitude);
        loc2.setLongitude(p2.longitude);
        //distance
        float distance = loc1.distanceTo(loc2);
        return distance;
    }

    private void removeRepeats()
    {
        for(int i = Globals.getListOfLocations().size()-1; i > 0;--i)
        {
            if(Globals.getListOfLocations().get(i).equals(Globals.getListOfLocations().get(i - 1)))
            {
                Globals.getListOfLocations().remove(i);
            }
        }
    }

    private float totalDistance()
    {
        float d = 0;
        for(int i = 0; i < Globals.getListOfLocations().size()-2; ++i)
        {
            d+=distanceBetweenLatLngs(Globals.getListOfLocations().get(i), Globals.getListOfLocations().get(i+1));
        }
        return d;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        returnRescourses();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void returnRescourses()
    {
        if (wakeLock.isHeld())
            wakeLock.release();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mRequestLocationUpdatesPendingIntent);
        mRequestLocationUpdatesPendingIntent.cancel();
        mNotifyMgr.cancel(001);
    }

    private void setUpNotification()
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.sound_chaser_icon)
                        .setContentTitle("SoundChaser")
                        .setContentText("Chase Away!");
        final Intent resultIntent = new Intent(this, OurGoogleMap.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setOngoing(true);
        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

}