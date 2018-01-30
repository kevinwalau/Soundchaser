package cs48.soundchaser;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores data associated with user locally on device so user
 * information is saved across sessions
 */
public class UserLocalStore {
    public static final String SP_NAME = "userDetails";
    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    public void storeUserData(User user) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("firstname lastname", user.getName());
        spEditor.putString("dobMonth", user.getDobMonth());
        spEditor.putString("dobDay", user.getDobDay());
        spEditor.putString("dobYear", user.getDobYear());
        spEditor.putString("heightFt", user.getHeightFt());
        spEditor.putString("heightIn", user.getHeightIn());
        spEditor.putString("weight", user.getWeight());
        spEditor.apply();
    }

    public User getUserData() {
        String name = userLocalDatabase.getString("firstname lastname", "");
        String dobMonth = userLocalDatabase.getString("dobMonth", "");
        String dobDay = userLocalDatabase.getString("dobDay", "");
        String dobYear = userLocalDatabase.getString("dobYear", "");
        String heightFt = userLocalDatabase.getString("heightFt", "");
        String heightIn = userLocalDatabase.getString("heightIn", "");
        String weight = userLocalDatabase.getString("weight", "");
        User storedUser;
        storedUser = new User(name, dobMonth, dobDay, dobYear, heightFt, heightIn, weight);
        return storedUser;
    }

    public void setIsProfileCreated(boolean isProfileCreated) {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("isProfileCreated", isProfileCreated);
        spEditor.apply();
    }

    public boolean getIsProfileCreated() {
        boolean isProfileCreated;
        isProfileCreated = userLocalDatabase.getBoolean("isProfileCreated", false);
        return isProfileCreated;
    }

    /* NOT NEEDED YET

    public void clearUserData() {
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.apply();
    }*/
}
