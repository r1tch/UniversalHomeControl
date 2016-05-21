package hu.evolver.uhc.comm;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONObject;

import hu.evolver.uhc.ui.PrefWrap;

/**
 * Created by rits on 2016-05-21.
 */
public class ScreenWaker {
    Context context = null;
    PrefWrap prefWrap;

    public ScreenWaker(Context context) {
        this.context = context;
        prefWrap = new PrefWrap(context);
    }

    public void newUpdate(final @NonNull String msg, final @NonNull JSONObject jsonObject) {
        if ("ZoneOpen".equals(msg))
            zoneOpen(jsonObject.optInt("zoneid", -1));
    }

    public void zoneOpen(int zoneId) {
        if (prefWrap.wakeup() && zoneId == prefWrap.wakeup_zone())
            wakeUp();
    }

    public void wakeUp() {
        Log.d("ScreenWaker", "Waking screen up...");

        // deprecated - we used to target HoneyComb as well
        // KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        // km.newKeyguardLock("RemoteWakeup").disableKeyguard();

        Context context = this.context;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wakeLock.acquire();
        wakeLock.release(); // we just want the screen to turn on, that's all
    }
}
