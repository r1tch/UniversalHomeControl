package hu.evolver.uhc.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import hu.evolver.uhc.R;
import hu.evolver.uhc.comm.UhcConnectivityService;
import hu.evolver.uhc.comm.UhcTcpEncoder;
import hu.evolver.uhc.model.UhcState;

public class MainActivity extends AppCompatActivity {
    private UhcConnectivityService uhcConnectivityService = null;
    private PrefWrap prefWrap = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    public FragmentHolder fragmentHolder = new FragmentHolder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context applicationContext = getApplicationContext();
//        applicationContext.startService(UhcConnectivityService.getConnectIntent(applicationContext));
        prefWrap = new PrefWrap(applicationContext);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_lightbulb_outline_white_24dp);    // TODO move to fragment to keep setup at one place
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_line_weight_white_24dp);    // TODO move to fragment to keep setup at one place
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_speaker_white_24dp);    // TODO move to fragment to keep setup at one place
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
            Log.d("ServiceConnection", "onServiceConnected");
            UhcConnectivityService.LocalBinder localBinder =
                    (UhcConnectivityService.LocalBinder) service;
            uhcConnectivityService = localBinder.getService();
            uhcConnectivityService.setMainActivity(MainActivity.this);
            if (fragmentHolder.lightsFragment != null) {
                uhcConnectivityService.getUhcState().addListener(fragmentHolder.lightsFragment);
                Log.d("MainActivity", "calling recreateSwitches");
                fragmentHolder.lightsFragment.recreateSwitches();
            } else
                Log.d("MainActivity", "fragmentHolder.lightsFragment is null");

            if (fragmentHolder.shadesFragment != null) {
                uhcConnectivityService.getUhcState().addListener(fragmentHolder.shadesFragment);
                fragmentHolder.shadesFragment.recreateSwitches();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection", "onServiceDisconnected");
            uhcConnectivityService.getUhcState().removeListener(fragmentHolder.lightsFragment);
            uhcConnectivityService.getUhcState().removeListener(fragmentHolder.shadesFragment);
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

    public UhcState getUhcState() {
        if (uhcConnectivityService != null)
            return uhcConnectivityService.getUhcState();

        return null;
    }

    public UhcTcpEncoder getEncoder() {
        if (uhcConnectivityService == null)
            return null;

        return uhcConnectivityService.getEncoder();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return LightsShadesFragment.newInstance(LightsShadesFragment.NodeType.Light);
                case 1:
                    return LightsShadesFragment.newInstance(LightsShadesFragment.NodeType.Shade);
                case 2:
                    return TempFragment.newInstance("Media");

            }
            return null;    // we should never-ever get here though
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";      // only icons
        }
    }
}
