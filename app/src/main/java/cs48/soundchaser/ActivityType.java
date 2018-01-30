package cs48.soundchaser;

/**
 * Created by Krassi on 2/22/2016.
 */
public enum ActivityType {
    WALKING(3,30,5,10,5, Icon.WALK_MAN),
    RUNNING(11,55,5,5,3, Icon.RUN_MAN),
    BIKING(20,60,5,3,1, Icon.BIKER),
    DEFAULT(15,15,5,1,1, Icon.HIKER);

    private double validSpeed; //m/s
    private double validDisp; //m
    private float smallestDisp; //m
    private long interval; //ms
    private long fastestInterval; //ms
    private Icon icon;

    ActivityType(double validSpeed, double validDisp, float smallestDisp, long interval, long fastestInterval, Icon icon) {
        this.validSpeed = validSpeed;
        this.validDisp = validDisp;
        this.smallestDisp = smallestDisp;
        this.interval = interval * 1000;
        this.fastestInterval = fastestInterval * 1000;
        this.icon = icon;
    }

    public Icon getIcon(){return icon;}

    public double getValidSpeed() {
        return validSpeed;
    }

    public double getValidDisp() {
        return validDisp;
    }

    public float getSmallestDisp() {
        return smallestDisp;
    }

    public long getInterval() {
        return interval;
    }

    public long getFastestInterval() {
        return fastestInterval;
    }
}
