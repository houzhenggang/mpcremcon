package com.mpcremcon.ui;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.mpcremcon.R;
import com.mpcremcon.localdb.LocalSettings;

/**
 * Created by Oleh Chaplya on 03.11.2014.
 */
public class PrefActivity extends PreferenceActivity {

    EditTextPreference ipaddress;
    EditTextPreference port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);

        ipaddress = (EditTextPreference)findPreference("ipaddress");
        ipaddress.setSummary(LocalSettings.getIPAddress());
        ipaddress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String p = o.toString().trim();
                LocalSettings.setIPAddress(p);
                ipaddress.setSummary(LocalSettings.getIPAddress());
                return true;
            }
        });

        port = (EditTextPreference)findPreference("port");
        port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String p = o.toString().trim();
                LocalSettings.setPort(p);
                port.setSummary(LocalSettings.getPort());
                return true;
            }
        });
        port.setSummary(LocalSettings.getPort());
    }



    protected void onResume() {
        super.onResume();
    }
}
