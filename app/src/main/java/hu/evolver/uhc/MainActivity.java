package hu.evolver.uhc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private UhcConnectivityService uhcConnectivityService = null;
    private PrefWrap prefWrap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context applicationContext = getApplicationContext();
//        applicationContext.startService(UhcConnectivityService.getConnectIntent(applicationContext));
        prefWrap = new PrefWrap(applicationContext);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, UhcConnectivityService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        uhcConnectivityService = null; // should not be necessary (serviceConnection does it)
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UhcConnectivityService.LocalBinder localBinder =
                    (UhcConnectivityService.LocalBinder)service;
            uhcConnectivityService = localBinder.getService();
            uhcConnectivityService.setMainActivity(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            uhcConnectivityService.unsetMainActivity();
            uhcConnectivityService = null;
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");

        if (uhcConnectivityService != null && !prefWrap.wakeup())
            uhcConnectivityService.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
        // TODO this is oversimplified; reconnect will trigger a status update on all uhc services
        Context applicationContext = getApplicationContext();
        applicationContext.startService(UhcConnectivityService.getReconnectIntent(applicationContext));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    public void startStopService(View view) {
        // TODO this should be fixed:
        // - auto connect on startup depending on setting
        // - disconnect when exiting (when activity goes out of focus)
        CheckBox checkBox = (CheckBox) view;
        Log.d("CLICKED", "Checkbox isChecked:" + checkBox.isChecked());

        Context context = getApplicationContext();
        if (checkBox.isChecked()) {
            context.startService(UhcConnectivityService.getConnectIntent(context));
        } else {
            context.stopService(UhcConnectivityService.getConnectIntent(context));
        }

    }
    */

    public void onTcpConnected() {
        // TODO update icon
    }
}
