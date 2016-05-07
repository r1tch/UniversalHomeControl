package hu.evolver.uhc.comm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import hu.evolver.uhc.model.UhcState;
import hu.evolver.uhc.ui.MainActivity;
import hu.evolver.uhc.ui.MainActivityDispatcher;
import hu.evolver.uhc.ui.PrefWrap;

public class UhcConnectivityService extends Service implements SimpleTcpClient.Listener {
    public static final String ACTION_CONNECT = "hu.evolver.uhc.ACTION_CONNECT";
    public static final String ACTION_RECONNECT = "hu.evolver.uhc.ACTION_RECONNECT";

    private Handler handler = null;
    private final LocalBinder localBinder = new LocalBinder();
    private SimpleTcpClient simpleTcpClient = new SimpleTcpClient(this);
    private MainActivityDispatcher mainActivityDispatcher = new MainActivityDispatcher();
    private PrefWrap prefWrap = null;
    private UhcState uhcState = new UhcState(mainActivityDispatcher);
    private UhcTcpEncoder uhcTcpEncoder = new UhcTcpEncoder(simpleTcpClient);

    @NonNull
    public static Intent getConnectIntent(Context context) {
        return getIntent(context, ACTION_CONNECT);
    }

    @NonNull
    public static Intent getReconnectIntent(Context context) {
        return getIntent(context, ACTION_RECONNECT);
    }

    public UhcState getUhcState() {
        return uhcState;
    }

    public UhcTcpEncoder getEncoder() {
        return uhcTcpEncoder;
    }

    public void setMainActivity(MainActivity mainActivity) {
        mainActivityDispatcher.mainActivity = mainActivity;
    }

    public void unsetMainActivity() {
        mainActivityDispatcher.mainActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("UhcConnectivityService", "onDestroy");
        simpleTcpClient.disconnect();
        handler = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_STICKY;  // ???

        Log.d("UhcConnectivityService", "onStartCommand: " + intent.getAction());

        if (intent.getAction().equals(ACTION_RECONNECT))
            simpleTcpClient.disconnect();

        if (prefWrap.hasConnectionSettings())
            simpleTcpClient.connect(prefWrap.host(), prefWrap.port());

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("UhcConnectivityService", "onCreate");

        handler = new Handler();
        prefWrap = new PrefWrap(getApplicationContext());
    }

    @NonNull
    private static Intent getIntent(Context context, String action) {
        Intent intent = new Intent(context, UhcConnectivityService.class);
        intent.setAction(action);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("UhcConnectivityService", "onBind " + intent.toString());
        return localBinder;
    }

    public void disconnect() {
        simpleTcpClient.disconnect();
    }

    public class LocalBinder extends Binder {
        public UhcConnectivityService getService() {
            return UhcConnectivityService.this;
        }
    }

    public void sendTcpMsg(final String msg) {
        simpleTcpClient.send(msg);
    }

    //////////////////////////////// thread functions /////////////////////////////
    @Override
    public void onTcpConnected() {
        Log.d("UhcConnectivityService", "Connected.");
        mainActivityDispatcher.dispatch(new Runnable() {
            public void run() {
                mainActivityDispatcher.mainActivity.onTcpConnected();
            }
        });
    }

    @Override
    public void onTcpMessage(String message) {
        // TODO ScreenWaker class - after uhc.py has Paradox support
        if (message.startsWith("on")) {
            // deprecated - we used to target HoneyComb as well
            // KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            // km.newKeyguardLock("RemoteWakeup").disableKeyguard();

            Context context = getApplicationContext();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
            wakeLock.release(); // we just want the screen to turn on, that's all
        }
        try {
            final JSONObject jsonObject = new JSONObject(message);
            final String msg = jsonObject.optString("msg");
            if (msg == null) {
                Log.e("UhcConnectivityService", "Missing msg from JSON:" + message);
                return;
            }

            uhcState.newUpdate(msg, jsonObject);
        } catch (JSONException e) {
            Log.e("UhcConnectivityService", "Unexpected JSON:" + message);
        }
    }

    @Override
    public void onTcpDisconnected(boolean perRequest) {
        Log.d("UhcConnectivityService", "Disconnected.");
        // TODO notify notification
        if (!perRequest)
            scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (handler == null || !prefWrap.hasConnectionSettings())
            return;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("UhcConnectivityService", "Reconnecting..");
                simpleTcpClient.connect(prefWrap.host(), prefWrap.port());
            }
        }, 1000);

    }
}
