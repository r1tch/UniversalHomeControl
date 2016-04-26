package hu.evolver.uhc;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

/**
 * Created by rits on 2016-04-26.
 */
public class LightsFragment extends Fragment {
    public LightsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lights, container, false);

        Context context = getContext();
        MainActivity mainActivity = (MainActivity)context;
        mainActivity.fragmentHolder.lightsFragment = this;
        // TODO create switches of lights
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity mainActivity = (MainActivity)getContext();
        mainActivity.fragmentHolder.lightsFragment = null;
    }

    public void recreateSwitches() {
        // TODO clear list
        MainActivity mainActivity = (MainActivity)getContext();
        JSONObject states = mainActivity.getUhcStateFor("gotNodes");

    }
}
