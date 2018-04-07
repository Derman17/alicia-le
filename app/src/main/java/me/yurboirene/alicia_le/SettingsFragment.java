package me.yurboirene.alicia_le;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.view.ContextThemeWrapper;
import android.util.TypedValue;

import me.yurboirene.alicia_le.common.DatabaseHelper;

public class SettingsFragment extends PreferenceFragmentCompat {


    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity();

        PreferenceScreen rootPreferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(rootPreferenceScreen);

        CharSequence[] entries = new CharSequence[0];
        try {
            entries = DatabaseHelper.getInstance().getRegionsNames();
        } catch (GettingDataException e) {
            e.printStackTrace();
            return;
        }

        CharSequence[] entryValues = new CharSequence[entries.length];

        for (int i = 1; i < entries.length + 1; i++) {
            entryValues[i - 1] = String.valueOf(i);
        }

        TypedValue themeTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.preferenceTheme, themeTypedValue, true);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, themeTypedValue.resourceId);

        // We instance each Preference using our ContextThemeWrapper object
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle("Category test");

        ListPreference listPref = new ListPreference(contextThemeWrapper);
        listPref.setKey("listPref");
        listPref.setTitle("ListPref test");
        listPref.setEntries(entries);
        listPref.setEntryValues(entryValues);

        getPreferenceScreen().addPreference(preferenceCategory);

        preferenceCategory.addPreference(listPref);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
