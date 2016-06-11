package hu.evolver.uhc.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefWrap {
    private SharedPreferences sharedPreferences = null;

    public PrefWrap(Context applicationContext) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    public String host() {
        return sharedPreferences.getString("pref_host", "").trim();
    }

    public int port() {
        return getIntPref("pref_port");
    }

    public boolean hasConnectionSettings() {
        return !host().isEmpty() && port() != 0;
    }

    public boolean wakeup() {
        return sharedPreferences.getBoolean("pref_wakeup", false);
    }

    public int wakeup_zone() { return getIntPref("pref_wakeup_zone"); }

    private int getIntPref(String key) {
        String strValue = sharedPreferences.getString(key, null);
        if (strValue != null) {
            try {
                return Integer.valueOf(strValue);
            } catch (NumberFormatException e) {
            }
        }

        return 0;
    }
}
