package hu.rits.remotewakeup;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;

public class WakeupService extends Service implements SimpleTcpClient.Listener {
    public static final String ACTION_CONNECT = "hu.rits.remotewakeup.ACTION_CONNECT";
    public static final String ACTION_DISCONNECT = "hu.rits.remotewakeup.ACTION_DISCONNECT";
    public static final String ACTION_TEST = "hu.rits.remotewakeup.ACTION_TEST"; // TODO remove later

    private Handler handler;
    private SimpleTcpClient simpleTcpClient = new SimpleTcpClient("10.0.1.2", 9998, this);

    @NonNull
    static Intent getConnectIntent(Context context) {
        return getIntent(context, ACTION_CONNECT);
    }

    @NonNull
    static Intent getDisconnectIntent(Context context) {
        return getIntent(context, ACTION_DISCONNECT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("WakeupService", "onDestroy");
        simpleTcpClient.disconnect();
        handler = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WakeupService", "onStartCommand - TODO start network thread here");

        simpleTcpClient.connect();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("WakeupService", "onCreate");

        handler = new Handler();
    }

    @NonNull
    static Intent getTestIntent(Context context) {
        return getIntent(context, ACTION_TEST);
    }

    @NonNull
    private static Intent getIntent(Context context, String action) {
        Intent intent = new Intent(context, WakeupService.class);
        intent.setAction(action);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("WakeupService", "onBind " + intent.toString());
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected() {
        // TODO notify notification
        Log.d("WakeupService", "Connected.");
    }

    @Override
    public void onMessage(String message) {
        if (message.startsWith("on")) {
            // deprecated but we target HoneyComb as well
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            km.newKeyguardLock("RemoteWakeup").disableKeyguard();

            Context context = getApplicationContext();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
            wakeLock.release(); // we just want the screen to turn on, that's all
        }
    }

    @Override
    public void onDisconnected(boolean perRequest) {
        Log.d("WakeupService", "Disconnected.");
        // TODO notify notification
        if (!perRequest)
            scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (handler == null)
            return;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("WakeupService", "Reconnecting..");
                simpleTcpClient.connect();
            }
        }, 1000);

    }
}
