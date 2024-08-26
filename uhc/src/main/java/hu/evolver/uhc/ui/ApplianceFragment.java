package hu.evolver.uhc.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

import hu.evolver.uhc.R;
import hu.evolver.uhc.comm.UhcTcpSender;
import hu.evolver.uhc.model.AirconditionerState;
import hu.evolver.uhc.model.JsonState;
import hu.evolver.uhc.model.KodiItem;
import hu.evolver.uhc.model.KodiPlayers;
import hu.evolver.uhc.model.StateUpdateListener;
import hu.evolver.uhc.model.UhcState;

/**
 * Created by rits on 2016-06-08.
 */
public class ApplianceFragment extends Fragment implements StateUpdateListener {
    private boolean isCreated = false;
    private Handler acSenderHandler = null;
    JsonState uiJsonState = new JsonState(new JSONObject());  // easiest way to remember button states (can't query highlights...)

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("ApplianceFragment", "onCreateView");

        acSenderHandler = new Handler();
        isCreated = true;

        View rootView = inflater.inflate(R.layout.fragment_appliance, container, false);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        Log.d("MediaFragment", "onDestroyView");

        acSenderHandler.removeCallbacksAndMessages(null);
        acSenderHandler = null;
        isCreated = false;

        super.onDestroyView();

        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity == null)
            return;

        if (mainActivity.getUhcState() != null)
            mainActivity.getUhcState().removeListener(this);

        mainActivity.fragmentHolder.applianceFragment = null;
    }

    public void onUhcStateCreated(UhcState uhcState) {
        Log.d("MediaFragment", "onUhcStateCreated");
        if (!isCreated)
            return;

        uhcState.addListener(this);
        stateChanged(uhcState.getState());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("ApplianceFragment", "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        isCreated = true;


        Context context = getContext();
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.fragmentHolder.applianceFragment = this;

        UhcState uhcState = mainActivity.getUhcState();
        if (uhcState != null)
            onUhcStateCreated(uhcState);

        createCallbacks(view);
    }

    private void createCallbacks(View view) {
        createProjectorButtonCallbacks(view);
        createHifiButtonCallbacks(view);
        createAcButtonCallbacks(view);
    }

    private void createProjectorButtonCallbacks(View view) {
        final ImageButton button = (ImageButton) view.findViewById(R.id.projectorPowerImageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState = !uiJsonState.isProjectorOn();

                setHighlightOnImageButton(button, newState);

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.projectorOnOff(newState);
            }
        });
    }

    private void createHifiButtonCallbacks(View view) {
        final ImageButton button = (ImageButton) view.findViewById(R.id.hifiPowerImageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState = !uiJsonState.isHifiOn();

                setHighlightOnImageButton(button, newState);

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.hifiOnOff(newState);
            }
        });
    }

    private void createAcButtonCallbacks(View view) {
        // -------------------------------- Power
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.acPowerImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                current.setOn(!current.isOn());

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerOnOff(current.isOn());
            }
        });

        // -------------------------------- Function - auto
        imageButton = (ImageButton) view.findViewById(R.id.acFuncAImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.function() == 'A')
                    return;

                current.setFunction('A');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });
        // -------------------------------- Function - cooling
        imageButton = (ImageButton) view.findViewById(R.id.acFuncCImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.function() == 'C')
                    return;

                current.setFunction('C');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });
        // -------------------------------- Function - heating
        imageButton = (ImageButton) view.findViewById(R.id.acFuncHImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.function() == 'H')
                    return;

                current.setFunction('H');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });
        // -------------------------------- Function - drying
        imageButton = (ImageButton) view.findViewById(R.id.acFuncDImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.function() == 'D')
                    return;

                current.setFunction('D');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });
        // -------------------------------- Function - fan
        imageButton = (ImageButton) view.findViewById(R.id.acFuncFImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.function() == 'F')
                    return;

                current.setFunction('F');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Temp - increase
        imageButton = (ImageButton) view.findViewById(R.id.acTempPlusImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.temperature() >= current.maxTemperature())
                    return;

                current.setTemperature(current.temperature() + 1);

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Temp - decrease
        imageButton = (ImageButton) view.findViewById(R.id.acTempMinusImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.temperature() <= current.minTemperature())
                    return;

                current.setTemperature(current.temperature() - 1);

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Fan - auto
        Button button = (Button) view.findViewById(R.id.acFanAButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.fan() == 'A')
                    return;

                current.setFan('A');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Fan - low
        button = (Button) view.findViewById(R.id.acFanLButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.fan() == 'L')
                    return;

                current.setFan('L');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Fan - auto
        button = (Button) view.findViewById(R.id.acFanMButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.fan() == 'M')
                    return;

                current.setFan('M');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Fan - auto
        button = (Button) view.findViewById(R.id.acFanHButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.fan() == 'H')
                    return;

                current.setFan('H');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Swing
        imageButton = (ImageButton) view.findViewById(R.id.acSwingImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                current.setSwing(!current.isSwingOn());

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Flags - quiet
        imageButton = (ImageButton) view.findViewById(R.id.acFlagQImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.flag() == 'Q')
                    current.setFlag('N');
                else
                    current.setFlag('Q');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });

        // -------------------------------- Flags - supersaver
        imageButton = (ImageButton) view.findViewById(R.id.acFlagSImageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AirconditionerState current = uiJsonState.getAirconditionerState();
                if (current.flag() == 'S')
                    current.setFlag('N');
                else
                    current.setFlag('S');

                UhcTcpSender sender = getUhcTcpSender();
                if (sender != null)
                    sender.airconditionerMode(current);
            }
        });
    }

    private UhcTcpSender getUhcTcpSender() {
        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity == null)
            return null;

        return mainActivity.getUhcTcpSender();
    }

    private UhcState getUhcState() {
        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity == null)
            return null;

        return mainActivity.getUhcState();
    }

    private void setHighlightOnImageButton(@NonNull View rootView, int imageButtonId, boolean shouldHighlight) {
        ImageButton button = (ImageButton) rootView.findViewById(imageButtonId);
        if (button == null)
            return;

        setHighlightOnImageButton(button, shouldHighlight);
    }

    private void setHighlightOnButton(@NonNull View rootView, int buttonId, boolean shouldHighlight) {
        Button button = (Button) rootView.findViewById(buttonId);
        if (button == null)
            return;

        setHighlightOnButton(button, shouldHighlight);
    }

    private void setHighlightOnImageButton(final ImageButton button, boolean shouldHighlight) {
        if (shouldHighlight)
            button.setColorFilter(getResources().getColor(R.color.colorAccent));
        else
            button.setColorFilter(0);
    }

    private void setHighlightOnButton(final Button button, boolean shouldHighlight) {
        if (shouldHighlight)
            button.setTextColor(getResources().getColor(R.color.colorAccent));
        else
            button.setTextColor(getResources().getColor(android.R.color.primary_text_light));
    }

    @Override
    public void zWaveGotNodes() {
    }

    @Override
    public void zWaveChangedLevels(int nodeId, int newLevel) {
    }

    @Override
    public void kodiVolumeChanged(boolean isMuted, double volumePercent) {
    }

    @Override
    public void kodiPlayerUpdate(KodiPlayers.Player player) {
    }

    @Override
    public void kodiPlayingItem(String label) {
    }

    @Override
    public void kodiOnStop() {
    }

    @Override
    public void kodiClearAudioPlaylist() {
    }

    @Override
    public void kodiPlaylistUpdate(ArrayList<KodiItem> items) {
    }

    @Override
    public void kodiAddAudioPlaylistItem(int position, int songid) {
    }

    @Override
    public void kodiRemoveAudioPlaylistItem(int position) {
    }

    @Override
    public void stateChanged(JsonState state) {
        if (state == null)
            return;

        uiJsonState = state.clone();

        View rootView = getView();
        if (rootView == null)
            return;

        ImageButton hifiPowerImageButton = (ImageButton) rootView.findViewById(R.id.hifiPowerImageButton);
        setHighlightOnImageButton(hifiPowerImageButton, state.isHifiOn());

        ImageButton projectorPowerImageButton = (ImageButton) rootView.findViewById(R.id.projectorPowerImageButton);
        setHighlightOnImageButton(projectorPowerImageButton, state.isProjectorOn());

        AirconditionerState acState = state.getAirconditionerState();
        updateAirconditionerDisplay(rootView, acState);
    }

    public void updateAirconditionerDisplay(@NonNull View rootView, AirconditionerState acState) {
        ImageButton acPowerImageButton = (ImageButton) rootView.findViewById(R.id.acPowerImageButton);
        setHighlightOnImageButton(acPowerImageButton, acState.isOn());

        LinearLayout acLayout = (LinearLayout) rootView.findViewById(R.id.acSettingsLayout);
        acLayout.setVisibility(acState.isOn() ? View.VISIBLE : View.GONE);

        updateACFunctionDisplay(rootView, acState);
        updateACTemperatureDisplay(rootView, acState.temperature());
        updateACFanDisplay(rootView, acState);
        updateACFlagDisplay(rootView, acState);
    }

    public void updateACFunctionDisplay(@NonNull View rootView, AirconditionerState acState) {
        setHighlightOnImageButton(rootView, R.id.acFuncAImageButton, acState.function() == 'A');
        setHighlightOnImageButton(rootView, R.id.acFuncCImageButton, acState.function() == 'C');
        setHighlightOnImageButton(rootView, R.id.acFuncDImageButton, acState.function() == 'D');
        setHighlightOnImageButton(rootView, R.id.acFuncFImageButton, acState.function() == 'F');
        setHighlightOnImageButton(rootView, R.id.acFuncHImageButton, acState.function() == 'H');
    }

    public void updateACTemperatureDisplay(@NonNull View rootView, int temperature) {
        LinearLayout acTempLayout = (LinearLayout) rootView.findViewById(R.id.acTempLayout);
        acTempLayout.setVisibility(temperature > 0 ? View.VISIBLE : View.GONE);

        if (temperature > 0) {
            TextView temperatureTextView = (TextView) rootView.findViewById(R.id.acTempTextView);
            temperatureTextView.setText(Integer.toString(temperature) + " ºC");
        }
    }

    public void updateACFanDisplay(@NonNull View rootView, AirconditionerState acState) {
        LinearLayout acFanLayout = (LinearLayout) rootView.findViewById(R.id.acFanLayout);
        acFanLayout.setVisibility(acState.function() == 'A' ? View.GONE : View.VISIBLE);

        setHighlightOnButton(rootView, R.id.acFanAButton, acState.fan() == 'A');
        setHighlightOnButton(rootView, R.id.acFanHButton, acState.fan() == 'H');
        setHighlightOnButton(rootView, R.id.acFanLButton, acState.fan() == 'L');
        setHighlightOnButton(rootView, R.id.acFanMButton, acState.fan() == 'M');
    }

    private void showHideViewById(@NonNull View rootView, int id, boolean show) {
        View view = rootView.findViewById(id);
        if (view == null)
            return;

        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void updateACFlagDisplay(@NonNull View rootView, AirconditionerState acState) {
        final boolean mayShowFlags = acState.function() != 'F';
        showHideViewById(rootView, R.id.acFlagQImageButton, mayShowFlags);
        showHideViewById(rootView, R.id.acFlagSImageButton, mayShowFlags);

        setHighlightOnImageButton(rootView, R.id.acSwingImageButton, acState.isSwingOn());
        setHighlightOnImageButton(rootView, R.id.acFlagQImageButton, acState.flag() == 'Q');
        setHighlightOnImageButton(rootView, R.id.acFlagSImageButton, acState.flag() == 'S');
    }
}
