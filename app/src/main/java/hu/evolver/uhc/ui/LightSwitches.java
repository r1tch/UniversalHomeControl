package hu.evolver.uhc.ui;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import hu.evolver.uhc.comm.ZWaveTcpSender;
import hu.evolver.uhc.model.ZWaveNode;

/**
 * Created by rits on 2016-05-05.
 */
public class LightSwitches extends ZWaveList {

    public LightSwitches(Fragment fragment) {
        super(fragment);
    }

    public View createElement(MainActivity mainActivity, ZWaveNode node) {
        if (!node.isLight())
            return null;

        Switch sw = new Switch(mainActivity);
        final ZWaveTcpSender encoder = mainActivity.getEncoder();
        final int id = node.getId();

        sw.setText(node.getName());

        sw.setPadding(hpad, vpad, hpad, vpad);
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch thisSwitch = (Switch) v;
                Log.d("LightsShadesFragment", "onClick checked:" + thisSwitch.isChecked());
                encoder.zWaveSetLevel(id, thisSwitch.isChecked() ? 100 : 0);
                Log.d("LightsShadesFragment", "onClick returns");
            }
        });

        sw.setChecked(node.getLevel() > 0);

        return sw;
    }

    public void zWaveChangedLevels(int nodeId, int newLevel) {
        Switch sw = (Switch) switchMap.get(nodeId);
        if (sw == null)
            return;

        sw.setChecked(newLevel > 0);
    }


}
