package hu.evolver.uhc.comm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import hu.evolver.uhc.model.UhcState;
import hu.evolver.uhc.ui.MainActivity;
import hu.evolver.uhc.ui.MediaBrowserActivity;
import hu.evolver.uhc.ui.UiThreadDispatcher;
import hu.evolver.uhc.ui.PrefWrap;

public class UhcConnectivityService extends Service implements SimpleTcpClient.Listener {
    public static final String ACTION_CONNECT = "hu.evolver.uhc.ACTION_CONNECT";
    public static final String ACTION_RECONNECT = "hu.evolver.uhc.ACTION_RECONNECT";

    private Handler reconnectHandler = null;
    private final LocalBinder localBinder = new LocalBinder();
    private SimpleTcpClient simpleTcpClient = new SimpleTcpClient(this);
    private UiThreadDispatcher uiThreadDispatcher = new UiThreadDispatcher();
    private PrefWrap prefWrap = null;
    private UhcState uhcState = new UhcState(simpleTcpClient, uiThreadDispatcher);
    private ZWaveTcpSender zWaveTcpSender = new ZWaveTcpSender(simpleTcpClient);
    private UhcTcpSender uhcTcpSender = new UhcTcpSender(simpleTcpClient);
    private ScreenWaker screenWaker = null;

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

    public KodiConnection getKodiConnection() {
        return uhcState.getKodiConnection();
    }

    public ZWaveTcpSender getZWaveEncoder() {
        return zWaveTcpSender;
    }

    public UhcTcpSender getUhcTcpSender() {
        return uhcTcpSender;
    }

    public boolean isConnected() {
        return simpleTcpClient.isConnected();
    }

    public void setMainActivity(MainActivity mainActivity) {
        uiThreadDispatcher.mainActivity = mainActivity;
    }

    public void unsetMainActivity() {
        uiThreadDispatcher.mainActivity = null;
    }

    public void setMediaBrowserActivity(MediaBrowserActivity mediaBrowserActivity) {
        uiThreadDispatcher.mediaBrowserActivity = mediaBrowserActivity;
    }

    public void unsetMediaBrowserActivity() {
        uiThreadDispatcher.mediaBrowserActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("UhcConnectivityService", "onDestroy");
        simpleTcpClient.disconnect();
        reconnectHandler = null;
        screenWaker = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("UhcConnectivityService", "onStartCommand");
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

        reconnectHandler = new Handler();
        prefWrap = new PrefWrap(getApplicationContext());
        screenWaker = new ScreenWaker(getApplicationContext());
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
        uhcState.onTcpConnected();

        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            public void run() {
                uiThreadDispatcher.mainActivity.onTcpConnected();
            }
        });
    }

    @Override
    public void onTcpDisconnected(boolean perRequest) {
        Log.d("UhcConnectivityService", "Disconnected.");

        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            public void run() {
                uiThreadDispatcher.mainActivity.onTcpDisconnected();
            }
        });
        if (!perRequest)
            scheduleReconnect();
    }

    @Override
    public void onTcpMessage(String message) {
        try {
            final JSONObject jsonObject = new JSONObject(message);

            uhcState.newUpdate(jsonObject);
            screenWaker.newUpdate(jsonObject);
        } catch (JSONException e) {
            // TODO same buffering strategy here as in uhc.py
            Log.e("UhcConnectivityService", "Unexpected JSON:" + message);
            Log.e("UhcConnectivityService", "error:" + e.getMessage());
        }
    }

    private void scheduleReconnect() {
        if (reconnectHandler == null)
            return;

        reconnectHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("UhcConnectivityService", "Reconnecting..");
                simpleTcpClient.connect(prefWrap.host(), prefWrap.port());
            }
        }, 1000);

    }
}
