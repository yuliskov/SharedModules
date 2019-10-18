package com.liskovsoft.sharedutils.prefs;

import android.content.Context;
import android.content.SharedPreferences;

class SharedPreferencesBase {
    private final SharedPreferences mPrefs;
    protected final Context mContext;

    public SharedPreferencesBase(Context context, String prefName) {
        mContext = context;
        mPrefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    protected void putInt(String key, int val) {
        mPrefs.edit()
                .putInt(key, val)
                .apply();
    }

    protected int getInt(String key, int defVal) {
        return mPrefs.getInt(key, defVal);
    }

    protected void putBoolean(String key, boolean val) {
        mPrefs.edit()
                .putBoolean(key, val)
                .apply();
    }

    protected boolean getBoolean(String key, boolean defVal) {
        return mPrefs.getBoolean(key, defVal);
    }

    protected void putString(String key, String  val) {
        mPrefs.edit()
                .putString(key, val)
                .apply();
    }

    protected String getString(String key, String defVal) {
        return mPrefs.getString(key, defVal);
    }
}
