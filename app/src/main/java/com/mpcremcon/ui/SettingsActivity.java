package com.mpcremcon.ui;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.mpcremcon.R;
import com.mpcremcon.localdb.LocalSettings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Oleh Chaplya on 03.11.2014.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String IP_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static final String PORT_PATTERN = "^[1-6][0-9]{0,4}?";

    EditTextPreference ipaddress;
    EditTextPreference port;
    EditTextPreference about;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);

        ipaddress = (EditTextPreference)findPreference("ipaddress");
        ipaddress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String ip = o.toString().trim();

                if(validateIP(ip)) {
                    LocalSettings.setIPAddress(ip);
                    ipaddress.setSummary(LocalSettings.getIPAddress());
                } else {
                    ipaddress.setSummary(LocalSettings.getIPAddress());
                    return false;
                }

                return true;
            }
        });

        port = (EditTextPreference)findPreference("port");
        port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String p = o.toString().trim();

                if(validatePort(p)) {
                    LocalSettings.setPort(p);
                    port.setSummary(LocalSettings.getPort());
                } else {
                    port.setSummary(LocalSettings.getPort());
                    return false;
                }

                return true;
            }
        });

        port.setSummary(LocalSettings.getPort());
        ipaddress.setSummary(LocalSettings.getIPAddress());

        about = (EditTextPreference)findPreference("about");
        about.setDialogLayoutResource(R.layout.about);
        about.setPositiveButtonText("Ok");
        about.setNegativeButtonText(null);
    }

    /**
     * Validates IP address
     * @param ip String IP Address
     * @return true if ip is valid
     */
    private boolean validateIP(final String ip) {
        Pattern pattern = Pattern.compile(IP_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    /**
     * Validates port value
     * @param port String port
     * @return true if port is valid
     */
    private boolean validatePort(final String port) {
        Pattern pattern = Pattern.compile(PORT_PATTERN);
        Matcher matcher = pattern.matcher(port);
        return matcher.matches();
    }
}
