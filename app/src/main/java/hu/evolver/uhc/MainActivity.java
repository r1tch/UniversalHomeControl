package hu.evolver.uhc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");
        // TODO disconnect unless wake-up enabled
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
        // TODO connect (if not yet connected)
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

}
