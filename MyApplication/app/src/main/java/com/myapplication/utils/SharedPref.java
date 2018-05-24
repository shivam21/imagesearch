package com.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences("ImageSearch", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    public int getNumcolums() {
        return sharedPreferences.getInt("numcolumns", 2);
    }

    public void setNumcolums(int numcolums) {
        editor.putInt("numcolumns", numcolums);
        editor.apply();
    }
}
