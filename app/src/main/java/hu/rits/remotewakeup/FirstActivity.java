package hu.rits.remotewakeup;

import android.app.KeyguardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

public class FirstActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "hu.rits.remotewakeup.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startStopService(View view) {
        CheckBox checkBox = (CheckBox) view;
        Log.d("CLICKED", "Checkbox isChecked:" + checkBox.isChecked());

        Context context = getApplicationContext();
        if (checkBox.isChecked()) {
            context.startService(WakeupService.getConnectIntent(context));
        } else {
            context.stopService(WakeupService.getConnectIntent(context));
        }

    }
}
