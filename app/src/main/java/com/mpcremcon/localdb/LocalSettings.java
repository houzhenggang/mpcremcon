package com.mpcremcon.localdb;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Used only to store local configuration,
 * ipaddress and port of MPC server
 *
 * Created by Oleh Chaplya on 03.11.2014.
 */
public class LocalSettings {

    static SharedPreferences sp;

    /**
     * Inits SharedPreferences
     * @param c context from application
     */
    public static void init(Context c) {
        sp = PreferenceManager.getDefaultSharedPreferences(c);

        // check if settings exist
        if(getIPAddress().equals("192.168.0.100")) setIPAddress("192.168.0.100");
        if(getPort().equals("13579")) setPort("13579");
    }

    /**
     * Returns an IP address
     * @return an IP address of default 192.168.0.100
     */
    public static String getIPAddress() {
        return sp.getString("ipaddress", "192.168.0.100");
    }

    /**
     * Returns a port
     * @return port value or default 13579
     */
    public static String getPort() {
        return sp.getString("port", "13579");
    }

    /**
     * Saves ip address in local storage
     * @param ip IP address
     * @return true if success
     */
    public static Boolean setIPAddress(String ip) {
        return sp.edit().putString("ipaddress", ip).commit();
    }

    /**
     * Saves port in local storage
     * @param port Port
     * @return true if success
     */
    public static Boolean setPort(String port) {
        return sp.edit().putString("port", port).commit();
    }
}
