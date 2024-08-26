package hu.evolver.uhc.model;

import android.util.Log;

/**
 * Created by rits on 2016-06-08.
 */
public class AirconditionerState {
    static public final String DEFAULT_STATECODE = "NANA24";

    private boolean isOn = false;
    private StringBuilder stateCode = new StringBuilder(DEFAULT_STATECODE);

    public AirconditionerState(final boolean isOn, final String stateCode) {
        this.isOn = isOn;

        if (stateCode == null)
            return;

        this.stateCode = new StringBuilder(stateCode);
        if (!isValid()) {
            Log.e("AirconditionerState", "Invalid state specified: " + stateCode);
            this.stateCode = new StringBuilder(DEFAULT_STATECODE);
        }
    }

    public AirconditionerState(boolean isOn, char flag, char fan, boolean isSwingOn, char function, int temp) {
        this.isOn = isOn;

        stateCode = new StringBuilder();
        stateCode.append(flag);
        stateCode.append(fan);
        stateCode.append(isSwingOn ? 'S' : 'N');
        stateCode.append(function);
        stateCode.append(Integer.toString(temp));

        if (!isValid()) {
            Log.e("AirconditionerState", "Invalid state during assembly: " + stateCode);
            this.stateCode = new StringBuilder(DEFAULT_STATECODE);
        }
    }

    public String stateCode() {
        return stateCode.toString();
    }

    public boolean isValid() {
        if (stateCode.length() != 6)
            return false;

        char function = function();
        if ("ACHDF".indexOf(function) == -1)   // auto, cooling, heating, drying, fan
            return false;

        char fan = fan();
        if ("ALMH".indexOf(fan) == -1)      // auto, low, medium, high
            return false;

        char swing = swing();
        if ("SN".indexOf(swing) == -1)      // swing, noswing
            return false;

        char flag = flag();
        if ("QSN".indexOf(flag) == -1)      // quiet, saver, normal
            return false;


        if (function() == 'F' &&
                (flag() != 'N' || temperature() != 0))
            return false;

        if (temperature() == 0 && function() != 'F')
            return false;

        if (function() == 'F')
            return true;        // all below checks are for non-fan mode

        if (temperature() < 16 || temperature() > 30)
            return false;

        if (flag() == 'S' && temperature() < 24)
            return false;

        if (flag() == 'S' && function() == 'F')
            return false;

        if (function() == 'D' && temperature() < 18)
            return false;

        if (function() == 'A' && fan() != 'A')
            return false;

        return true;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean isOn) {
        this.isOn = isOn;
    }

    public char flag() {
        return stateCode.charAt(0);
    }

    public void setFlag(char flag) {
        if (flag == 'S') {
            if (function() == 'F')      // fan can't have super-saver enabled
                return;

            if (temperature() < 24)     // min temp for super-saver
                setTemperature(24);
        }

        if (flag == 'Q' && function() == 'F') // fan can't have quiet-mode enabled
            return;

        stateCode.setCharAt(0, flag);
    }

    public char fan() {
        return stateCode.charAt(1);
    }

    public void setFan(char fan) {
        if (function() == 'A')
            fan = 'A';

        stateCode.setCharAt(1, fan);
    }

    public char swing() {
        return stateCode.charAt(2);
    }

    public void setSwing(boolean shouldSwing) {
        stateCode.setCharAt(2, shouldSwing ? 'S' : 'N');
    }

    public boolean isSwingOn() {
        return swing() == 'S';
    }

    public char function() {
        return stateCode.charAt(3);
    }

    public void setFunction(char function) {
        if (temperature() == 0)
            setTemperature(23);

        switch (function) {
            case 'A':
                setFan('A');
                break;
            case 'D':
                if (temperature() < 18)
                    setTemperature(18);
                break;
            case 'F':
                setTemperature(0);
                setFlag('N');
                break;
        }

        stateCode.setCharAt(3, function);
    }

    public int temperature() {
        String tempString = stateCode.substring(4);
        if ("XX".equals(tempString))        // means fan mode is on -- no target temperature
            return 0;

        try {
            return Integer.parseInt(tempString);
        } catch (NumberFormatException e) {
            Log.e("AirconditionerState", "Could not parse temp from " + stateCode.toString());
            return 23;
        }
    }

    public void setTemperature(int temperature) {
        String tempString = null;
        if (temperature == 0)
            tempString = "XX";
        else
            tempString = String.format("%02d", temperature);

        stateCode = new StringBuilder(stateCode.substring(0, 4));
        stateCode.append(tempString);
    }

    public int maxTemperature() {
        if (function() == 'F')
            return 0;

        return 30;
    }

    public int minTemperature() {
        if (function() == 'F')
            return 0;

        if (function() == 'D')
            return 18;

        return 16;
    }
}
