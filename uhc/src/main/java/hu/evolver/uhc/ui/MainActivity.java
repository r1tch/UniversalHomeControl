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
import hu.evolver.uhc.comm.KodiConnection;
import hu.evolver.uhc.comm.UhcConnectivityService;
import hu.evolver.uhc.comm.UhcTcpSender;
import hu.evolver.uhc.comm.ZWaveTcpSender;
import hu.evolver.uhc.model.JsonState;
import hu.evolver.uhc.model.UhcState;

public class MainActivity extends AppCompatActivity {
    private enum PlayButtonState {Playing, Paused, OtherActive}     // OtherActive -> we can only stop it for now

    private UhcConnectivityService uhcConnectivityService = null;
    private PrefWrap prefWrap = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager = null;
    public FragmentHolder fragmentHolder = new FragmentHolder();
    private Menu optionsMenu = null;
    PlayButtonState playButtonState = PlayButtonState.Paused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context applicationContext = getApplicationContext();
        // applicationContext.startService(UhcConnectivityService.getConnectIntent(applicationContext));
        prefWrap = new PrefWrap(applicationContext);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_lightbulb_outline_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_line_weight_white_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_speaker_white_24dp);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_settings_remote_white_24dp);

        onTcpDisconnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        optionsMenu = menu;

        MenuItem playPauseMenuItem = menu.findItem(R.id.action_playpause);

        if (uhcConnectivityService == null || !uhcConnectivityService.isConnected()) {
            playPauseMenuItem.setVisible(false);
        }

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

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");

        if (uhcConnectivityService != null) {
            if (!prefWrap.wakeup())
                uhcConnectivityService.disconnect();

            UhcState uhcState = uhcConnectivityService.getUhcState();
            uhcState.removeAllListeners();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
        // TODO this is oversimplified; reconnect will trigger a status update on all uhc services
        Context applicationContext = getApplicationContext();
        applicationContext.startService(UhcConnectivityService.getReconnectIntent(applicationContext));
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ServiceConnection", "onServiceConnected");
            UhcConnectivityService.LocalBinder localBinder =
                    (UhcConnectivityService.LocalBinder) service;
            uhcConnectivityService = localBinder.getService();
            uhcConnectivityService.setMainActivity(MainActivity.this);

            UhcState uhcState = uhcConnectivityService.getUhcState();
            uhcState.removeAllListeners();      // that's needed here as disconnection is not received

            if (fragmentHolder.lightsFragment != null)
                fragmentHolder.lightsFragment.onUhcStateCreated(uhcState);

            if (fragmentHolder.shadesFragment != null)
                fragmentHolder.shadesFragment.onUhcStateCreated(uhcState);

            if (fragmentHolder.mediaFragment != null)
                fragmentHolder.mediaFragment.onUhcStateCreated(uhcState);

            if (fragmentHolder.applianceFragment != null)
                fragmentHolder.applianceFragment.onUhcStateCreated(uhcState);

            if (uhcConnectivityService.isConnected())
                onTcpConnected();
            else
                onTcpDisconnected();

            onStateChanged(uhcState.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection", "onServiceDisconnected");

            // IMPORTANT, this is not being called !!!
            removeListeners();
            uhcConnectivityService.unsetMainActivity();
            uhcConnectivityService = null;
        }
    };

    private void removeListeners() {
        UhcState uhcState = uhcConnectivityService.getUhcState();
        if (uhcState == null)
            return;

        uhcState.removeAllListeners();
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

        if (id == R.id.action_playpause) {
            if (uhcConnectivityService == null)
                return true;

            if (playButtonState == PlayButtonState.Paused) {
                uhcConnectivityService.getUhcTcpSender().hifiSetMediaSource();
                uhcConnectivityService.getKodiConnection().sendPlay();
                return true;
            }

            if (playButtonState == PlayButtonState.Playing) {
                uhcConnectivityService.getKodiConnection().sendPause();
                return true;
            }

            if (playButtonState == PlayButtonState.OtherActive) {
                uhcConnectivityService.getKodiConnection().stopOtherPlayersIfRequired();
                return true;
            }

            return true;
        }

        if (id == R.id.action_guestmode) {
            if (uhcConnectivityService == null)
                return true;

            // TODO check if this is the state after
            uhcConnectivityService.getUhcTcpSender().setGuestMode(item.isChecked());

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTcpConnected() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setLogo(R.drawable.ic_home_white_24dp);
        toolbar.setTitle("");

        if (optionsMenu != null) {
            MenuItem menuItem = optionsMenu.findItem(R.id.action_playpause);
            menuItem.setVisible(true);
        }
    }

    public void onTcpDisconnected() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setLogo(R.drawable.ic_autorenew_white_24dp);
        toolbar.setTitle(R.string.connecting);

        if (optionsMenu != null) {
            MenuItem menuItem = optionsMenu.findItem(R.id.action_playpause);
            menuItem.setVisible(false);
        }
    }

    public UhcState getUhcState() {
        if (uhcConnectivityService != null)
            return uhcConnectivityService.getUhcState();

        return null;
    }

    public ZWaveTcpSender getZWaveEncoder() {
        if (uhcConnectivityService == null)
            return null;

        return uhcConnectivityService.getZWaveEncoder();
    }

    public UhcTcpSender getUhcTcpSender() {
        if (uhcConnectivityService == null)
            return null;

        return uhcConnectivityService.getUhcTcpSender();
    }

    public KodiConnection getKodiConnection() {
        if (uhcConnectivityService == null)
            return null;

        return uhcConnectivityService.getKodiConnection();
    }

    public void onPlaying() {
        playButtonState = PlayButtonState.Playing;

        if (optionsMenu != null) {
            MenuItem menuItem = optionsMenu.findItem(R.id.action_playpause);

            menuItem.setIcon(R.drawable.ic_pause_white_24dp);
        }
    }

    public void onStoppedPaused() {
        playButtonState = PlayButtonState.Paused;

        if (optionsMenu != null) {
            MenuItem menuItem = optionsMenu.findItem(R.id.action_playpause);
            menuItem.setIcon(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    public void otherPlayerActive() {
        playButtonState = PlayButtonState.OtherActive;

        if (optionsMenu != null) {
            MenuItem menuItem = optionsMenu.findItem(R.id.action_playpause);
            menuItem.setIcon(R.drawable.ic_stop_white_24dp);
        }
    }

    public void onStateChanged(JsonState state) {
        if (state == null)
            return;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null)
            return;

        if (state.isAtHome())
            toolbar.setLogo(R.drawable.ic_home_white_24dp);
        else if (state.isGuestMode())
            toolbar.setLogo(R.drawable.ic_remove_circle_white_24dp);
        else if (state.isAsleep())
            toolbar.setLogo(R.drawable.ic_airline_seat_individual_suite_white_24dp);
        else
            toolbar.setLogo(R.drawable.ic_lock_white_24dp);

        if (optionsMenu != null) {
            MenuItem guestMode = optionsMenu.findItem(R.id.action_guestmode);
            guestMode.setChecked(state.isGuestMode());
        }
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
                    return new MediaFragment();
                case 3:
                    return new ApplianceFragment();

            }
            return null;    // we should never-ever get here though
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";      // only icons
        }
    }
}
