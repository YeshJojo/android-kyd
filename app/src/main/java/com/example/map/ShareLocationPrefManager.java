package com.example.map;

import android.content.Context;
import android.content.SharedPreferences;

public class ShareLocationPrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "share-location";

    private static final String CHECKED = "SharedLocation";

    public ShareLocationPrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public void shareLocation(boolean isFirstTime) {
        editor.putBoolean(CHECKED, isFirstTime);
        editor.commit();
    }
    public boolean isChecked() {
        return pref.getBoolean(CHECKED, true);
    }
}