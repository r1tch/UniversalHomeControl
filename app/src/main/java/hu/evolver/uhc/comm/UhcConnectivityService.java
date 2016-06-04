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
    private UhcState uhcState = new UhcState(simpleTcpClient, mainActivityDispatcher);
    private ZWaveTcpSender ZWaveTcpSender = new ZWaveTcpSender(simpleTcpClient);
    private ScreenWaker screenWaker = null;

    private boolean isConnected = false;

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

    public ZWaveTcpSender getEncoder() {
        return ZWaveTcpSender;
    }

    public boolean isConnected() {
        return simpleTcpClient.isConnected();
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
        screenWaker = null;
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

        mainActivityDispatcher.dispatch(new Runnable() {
            public void run() {
                mainActivityDispatcher.mainActivity.onTcpConnected();
            }
        });
    }

    @Override
    public void onTcpDisconnected(boolean perRequest) {
        Log.d("UhcConnectivityService", "Disconnected.");

        mainActivityDispatcher.dispatch(new Runnable() {
            public void run() {
                mainActivityDispatcher.mainActivity.onTcpDisconnected();
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
        }
    }

    private void scheduleReconnect() {
        if (handler == null)
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
