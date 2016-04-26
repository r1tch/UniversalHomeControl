package hu.evolver.uhc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TempFragment extends Fragment {
    private static final String ARG_FRAGMENT_NAME = "fragment_name";

    public TempFragment() {
    }

    public static TempFragment newInstance(final String name) {
        TempFragment fragment = new TempFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FRAGMENT_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_temp, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        String theText = getArguments().getString(ARG_FRAGMENT_NAME);
        String theSubstitutedText = getString(R.string.temp_section_format, theText);
        Log.d("TempFragment", theSubstitutedText);
        textView.setText(theSubstitutedText);
        return rootView;
    }

}
