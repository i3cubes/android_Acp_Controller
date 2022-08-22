package com.i3cubes.acp_controller;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    public static final String PREFS_NAME = "I3C";
    public static final String DEVICE_ADRRESS = "ADDRESS";
    public static final String DEVICE_NAME = "NAME";

    public void setDeviceData(Context contx, String address,String name){
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings=contx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        editor=settings.edit();
        editor.putString(DEVICE_ADRRESS,address);
        editor.putString(DEVICE_NAME,name);
        editor.commit();
    }
    public String getDeviceAdrress(Context contx){
        SharedPreferences settings;
        settings=contx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);

        if(settings.contains(DEVICE_ADRRESS)){
            return (settings.getString(DEVICE_ADRRESS,null));
        }
        else{
            return null;
        }
    }
    public String getDeviceName(Context contx){
        SharedPreferences settings;
        settings=contx.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);

        if(settings.contains(DEVICE_NAME)){
            return (settings.getString(DEVICE_NAME,null));
        }
        else{
            return null;
        }
    }
}
