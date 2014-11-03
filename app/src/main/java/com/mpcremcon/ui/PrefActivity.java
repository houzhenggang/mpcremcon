package com.mpcremcon.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.mpcremcon.R;

/**
 * Created by Oleh Chaplya on 03.11.2014.
 */
public class PrefActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
    }


    protected void onResume() {
        super.onResume();
    }
}
