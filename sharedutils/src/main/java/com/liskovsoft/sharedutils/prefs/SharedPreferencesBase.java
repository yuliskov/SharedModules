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

    public void putLong(String key, long val) {
        mPrefs.edit()
                .putLong(key, val)
                .apply();
    }

    public long getLong(String key, long defVal) {
        return mPrefs.getLong(key, defVal);
    }

    public void putInt(String key, int val) {
        mPrefs.edit()
                .putInt(key, val)
                .apply();
    }

    public int getInt(String key, int defVal) {
        return mPrefs.getInt(key, defVal);
    }

    public void putBoolean(String key, boolean val) {
        mPrefs.edit()
                .putBoolean(key, val)
                .apply();
    }

    public boolean getBoolean(String key, boolean defVal) {
        return mPrefs.getBoolean(key, defVal);
    }

    public void putString(String key, String  val) {
        mPrefs.edit()
                .putString(key, val)
                .apply();
    }

    public String getString(String key, String defVal) {
        return mPrefs.getString(key, defVal);
    }
}
