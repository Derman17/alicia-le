package me.yurboirene.alicia_le;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

public class CustomListPref extends ListPreference {
    public CustomListPref (Context context, AttributeSet attrs) {
        super(context, attrs);

        setEntries(entries());
        setEntryValues(entryValues());
        setValueIndex(initializeIndex());
    }

    public CustomListPref (Context context) {
        this(context, null);
    }

    private CharSequence[] entries() {
        //action to provide entry data in char sequence array for list
        String myEntries[] = {"one", "two", "three", "four", "five"};

        return myEntries;
    }

    private CharSequence[] entryValues() {
        //action to provide value data for list

        String myEntryValues[] = {"ten", "twenty", "thirty", "forty", "fifty"};
        return myEntryValues;
    }

    private int initializeIndex() {
        //here you can provide the value to set (typically retrieved from the SharedPreferences)
        //...

        int i = 2;
        return i;
    }

}
