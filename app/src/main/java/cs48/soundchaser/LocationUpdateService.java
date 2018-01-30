package cs48.soundchaser;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationResult;

public class LocationUpdateService extends IntentService {

    private final String TAG = "LocationUpdateService";
    Location location;



    public LocationUpdateService() {

        super("LocationUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            location = locationResult.getLastLocation();
            if (location != null) {
                Intent i = new Intent("LocationChanged");
                sendLocationBroadcast(i);
                Log.d("locationtesting", "accuracy: " + location.getAccuracy() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            }
        }
    }

    private void sendLocationBroadcast(Intent intent){
        intent.putExtra("lat", getLocation().getLatitude());
        intent.putExtra("lon", getLocation().getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i("locationtesting", "location sent: " + " lat: " + location.getLatitude() + " lon: " + location.getLongitude());
    }

    public Location getLocation()
    {
        return location;
    }
}