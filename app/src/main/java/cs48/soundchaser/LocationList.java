package cs48.soundchaser;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Krassi on 2/25/2016.
 */
public class LocationList {
    int id;
    String time; //EEE, dd MMM yyyy 'at' hh:mm a
    String list; //lat-lon-lat-lon-...
    String distance; //meters
    String duration; //seconds

    public LocationList(){}

    public LocationList(int id, String time, String list, String distance, String duration) {
        this.id = id;
        this.list = list;
        this.distance = distance;
        this.duration = duration;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
