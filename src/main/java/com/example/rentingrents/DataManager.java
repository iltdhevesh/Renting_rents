package com.example.rentingrents;

import android.content.Context;

import android.content.SharedPreferences;

public class DataManager {

    private static final String USER_DATA_PREF = "UserData";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";

    private SharedPreferences preferences;

    public DataManager(Context context) {
        preferences = context.getSharedPreferences(USER_DATA_PREF, Context.MODE_PRIVATE);
    }

    public void saveUserData(String name, String phoneNumber) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    public String getUserName() {
        return preferences.getString(KEY_NAME, "");
    }

    public String getUserPhoneNumber() {
        return preferences.getString(KEY_PHONE_NUMBER, "");
    }
}

