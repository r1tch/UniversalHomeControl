package hu.evolver.uhc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefWrap {
    private SharedPreferences sharedPreferences = null;

    PrefWrap(Context applicationContext) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    String host() {
        return sharedPreferences.getString("pref_host", "");
    }

    int port() {
        return getIntPref("pref_port");
    }

    boolean hasConnectionSettings() {
        return !host().isEmpty() && port() != 0;
    }

    boolean wakeup() {
        return sharedPreferences.getBoolean("pref_wakeup", false);
    }

    private int getIntPref(String key){
        String strValue = sharedPreferences.getString(key, null);
        if (strValue != null){
            try {
                return Integer.valueOf(strValue);
            } catch (NumberFormatException e) {
            }
        }

        return 0;
    }
}
