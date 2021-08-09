package com.liskovsoft.sharedutils.locale;

import android.content.Context;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;

import java.util.Locale;
import java.util.StringTokenizer;

public class LangUpdater {
    private static final String TAG = LangUpdater.class.getSimpleName();
    private static Locale sCachedLocale;
    private final GlobalPreferences mPrefs;
    private final Context mContext;

    public LangUpdater(Context ctx) {
        mContext = ctx;
        mPrefs = GlobalPreferences.instance(ctx);
    }

    public void update() {
        String locale = getUpdatedLocale();

        Log.d(TAG, "Updating locale to " + locale);

        LangUpdaterHelper.forceLocale(mContext, locale);
    }

    public String getUpdatedLocale() {
        String locale = LangUpdaterHelper.guessLocale(mContext);

        String langCode = getPreferredLocale();

        langCode = appendCountry(langCode);

        // not set or default language selected
        if (langCode != null && !langCode.isEmpty()) {
            locale = langCode;
        }

        return locale;
    }

    /**
     * Get locale as lang code (e.g. zh, ru_RU etc)
     * @return lang code
     */
    public String getPreferredLocale() {
        String language = mPrefs.getPreferredLanguage();

        return language != null ? language : "";
    }

    /**
     * Get locale in http format (e.g. zh, ru-RU etc)<br/>
     * Example: <code>ru,en-US;q=0.9,en;q=0.8,uk;q=0.7</code>
     * @return lang code
     */
    public String getPreferredBrowserLocale() {
        String locale = getPreferredLocale();

        if (locale == null || locale.isEmpty()) {
            locale = LangUpdaterHelper.getDefaultLocale();
        }

        return locale.replace("_", "-").toLowerCase();
    }

    /**
     * E.g. ru, uk, en
     * @param langCode lang
     */
    public void setPreferredLocale(String langCode) {
        mPrefs.setPreferredLanguage(langCode);
    }

    public String getPreferredCountry() {
        String country = mPrefs.getPreferredCountry();
        return country != null ? country : "";
    }

    public void setPreferredCountry(String countryCode) {
        mPrefs.setPreferredCountry(countryCode);
    }

    private String appendCountry(String langCode) {
        if (langCode != null && !langCode.isEmpty()) {
            String preferredCountry = getPreferredCountry();

            if (preferredCountry != null && !preferredCountry.isEmpty()) {
                StringTokenizer tokenizer = new StringTokenizer(langCode, "_");
                String lang = tokenizer.nextToken();

                langCode = String.format("%s_%s", lang, preferredCountry);
            }
        }

        return langCode;
    }

    public static Locale getLocale(Context context) {
        if (sCachedLocale == null) {
            LangUpdater updater = new LangUpdater(context);
            String langCode = updater.getUpdatedLocale();
            sCachedLocale = LangUpdaterHelper.parseLangCode(langCode);
        }

        return sCachedLocale;
    }
}
