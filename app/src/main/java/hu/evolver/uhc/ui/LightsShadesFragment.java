package hu.evolver.uhc.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.util.Map;
import java.util.TreeMap;

import hu.evolver.uhc.R;
import hu.evolver.uhc.comm.UhcTcpEncoder;
import hu.evolver.uhc.model.StateUpdateListener;
import hu.evolver.uhc.model.UhcState;
import hu.evolver.uhc.model.ZWaveNode;

/**
 * Created by rits on 2016-04-26.
 */
public class LightsShadesFragment extends Fragment implements StateUpdateListener {
    private LinearLayout containerLayout = null;
    private Map<Integer, Switch> switchMap = new TreeMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("LightsShadesFragment", "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_lightsshades, container, false);

        Context context = getContext();
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.fragmentHolder.lightsFragment = this;
        if (mainActivity.getUhcState() != null)
            mainActivity.getUhcState().addListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        containerLayout = (LinearLayout) view.findViewById(R.id.the_switches);

        recreateSwitches();
    }

    @Override
    public void onDestroyView() {
        Log.d("LightsShadesFragment", "onDestroyView");
        super.onDestroyView();
        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.fragmentHolder.lightsFragment = null;
        if (mainActivity.getUhcState() != null)
            mainActivity.getUhcState().removeListener(this);
        containerLayout = null;
    }

    public void recreateSwitches() {
        if (containerLayout == null) {
            Log.d("LightsShadesFragment", "containerLayout null");
            return;
        }

        MainActivity mainActivity = (MainActivity) getContext();
        UhcState uhcState = mainActivity.getUhcState();
        if (uhcState == null) {
            Log.d("LightsShadesFragment", "uhcState null");
            return;
        }

        containerLayout.removeAllViews();
        switchMap.clear();

        Map<String, ZWaveNode> nodesByName = uhcState.getZWaveNodeStore().allByName();

        int hpad = (int)getResources().getDimension(R.dimen.activity_horizontal_margin);
        int vpad = (int)getResources().getDimension(R.dimen.activity_vertical_margin);

        for (Map.Entry<String, ZWaveNode> entry : nodesByName.entrySet()) {
            ZWaveNode node = entry.getValue();

            if (!node.isLight())    // TODO handle the shade too with this class?? but needs custom "switch"
                continue;

            Switch sw = new Switch(mainActivity);
            final UhcTcpEncoder encoder = mainActivity.getEncoder();
            final int id = node.getId();
            switchMap.put(id, sw);

            sw.setText(node.getName());

            sw.setPadding(hpad, vpad, hpad, vpad);
            sw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch thisSwitch = (Switch) v;
                    Log.d("LightsShadesFragment", "onClick checked:"+thisSwitch.isChecked());
                    encoder.zWaveSetLevel(id, thisSwitch.isChecked() ? 100 : 0);
                    Log.d("LightsShadesFragment", "onClick returns");
                }
            });

            sw.setChecked(node.getLevel() > 0);

            containerLayout.addView(sw);
        }
    }

    @Override
    public void zWaveGotNodes() {
        recreateSwitches();
    }

    @Override
    public void zWaveChangedLevels(int nodeId, int newLevel) {
        Switch sw = switchMap.get(nodeId);
        if (sw == null)
            return;

        sw.setChecked(newLevel > 0);
    }
}
