package cs48.soundchaser;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import java.util.Random;

/**
 * A random point that will be within a given distance (as the bird flies) of a given LatLng point
 *  on OurGoogleMap. Does not (currently?) need to be a "good" point (i.e. reachable by the user
 *  or within a specified walking distance). RandomPathGenerator will handle
 *  acceptance and rejection of points for a path.
 */

public class RandomPoint {
    private LatLng coordinates;
    private LatLng givenPoint;
    private double givenDist;
    private final static double ONE_DEGREE_LAT = 110.574; // km
    private final static double LNG_CONVERSION_CONST = 111.320; // km

    public RandomPoint(LatLng givenPoint, double givenDist) {
        this.givenPoint = givenPoint;
        this.givenDist = givenDist;
        this.coordinates = generatePoint(givenPoint, givenDist);
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public LatLng generatePoint(LatLng givenPoint, double givenDist) {
        double givenPointLat = givenPoint.latitude;
        double givenPointLng = givenPoint.longitude;
        double randLat = 0;
        double randLng = 0;
        // Find the approx. number of degrees latitude between givenPoint and the top point of
        // circle with radius of givenDist
        double degreesLat = givenDist / ONE_DEGREE_LAT;
        // Find the approx. number of degrees longitude between givenPoint and the rightmost
        // point of circle with radius of givenDist
        double radiansLat = Math.toRadians(degreesLat);
        double degreesLng = givenDist / (LNG_CONVERSION_CONST * Math.cos(radiansLat));

        // Add a random number within the top/bottom bound and rightmost/leftmost bound to givenPoint.
        Random randGen = new Random();
        double randY, randX;
        boolean isValidCoord = false;
        boolean isInCircle = false;
        while (!isValidCoord || !isInCircle) {
            randY = (randGen.nextDouble() * 2 - 1) * degreesLat;
            randX = (randGen.nextDouble() * 2 - 1) * degreesLng;

            randLat = (givenPointLat + randY);
            randLng = (givenPointLng + randX);

            /* DEBUG
            System.out.println("GIVEN PT:");
            System.out.println(givenPoint);
            System.out.println("GIVEN DIST:");
            System.out.println(givenDist);
            System.out.println("LAT:");
            System.out.println(randLat);
            System.out.println("LONG:");
            System.out.println(randLng);
             DEBUG */

            isValidCoord = isValidCoord(randLat, randLng);
            isInCircle   = isInCircle(randLat, randLng);
        }

        LatLng finalPt = new LatLng(randLat, randLng);
        return finalPt;

    }

    private boolean isValidCoord(double randLat, double randLng) {
        if (randLat < -90 || randLat > 90 || randLng < -180 || randLng > 180) { return false; }
        return true;
    }

    private boolean isInCircle(double randLat, double randLng) {
        double startLat = givenPoint.latitude;
        double startLng = givenPoint.longitude;
        float[] dist = new float[2];
        Location.distanceBetween(startLat, startLng, randLat, randLng, dist);
        /*System.out.println("CALCULATED DIST BTWN:");
        System.out.println(dist[0]); */
        float distInKm = dist[0]/1000;
        if (distInKm > givenDist) { return false; }
        return true;
    }

}