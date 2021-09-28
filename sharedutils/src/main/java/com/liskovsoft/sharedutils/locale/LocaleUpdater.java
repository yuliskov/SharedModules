package com.liskovsoft.sharedutils.locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.prefs.GlobalPreferences;

import java.util.Locale;
import java.util.StringTokenizer;

public class LocaleUpdater {
    private static final String TAG = LocaleUpdater.class.getSimpleName();
    private static Locale sCachedLocale;
    private final GlobalPreferences mPrefs;
    private final Context mContext;

    public LocaleUpdater(Context ctx) {
        mContext = ctx;
        mPrefs = GlobalPreferences.instance(ctx);
    }

    public void update() {
        String locale = getUpdatedLocale();

        Log.d(TAG, "Updating locale to " + locale);

        LocaleUpdaterHelper.forceLocale(mContext, locale);
    }

    public String getUpdatedLocale() {
        String locale = null;

        //locale = LocaleUpdaterHelper.guessLocale(mContext);

        String langCode = getPreferredLanguage();

        langCode = appendCountry(langCode);

        // not set or default language selected
        if (langCode != null && !langCode.isEmpty()) {
            locale = langCode;
        }

        return locale;
    }

    /**
     * Get locale in http format (e.g. zh, ru-RU etc)<br/>
     * Example: <code>ru,en-US;q=0.9,en;q=0.8,uk;q=0.7</code>
     * @return lang code
     */
    public String getPreferredBrowserLocale() {
        String locale = getPreferredLanguage();

        if (locale == null || locale.isEmpty()) {
            locale = LocaleUpdaterHelper.getDefaultLocale();
        }

        return locale.replace("_", "-").toLowerCase();
    }

    /**
     * Get locale as lang code (e.g. zh, ru_RU etc)
     * @return lang code
     */
    public String getPreferredLanguage() {
        String language = mPrefs.getPreferredLanguage();

        return language != null ? language : "";
    }

    /**
     * E.g. ru, uk, en
     * @param langCode lang
     */
    public void setPreferredLanguage(String langCode) {
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

    public static Locale getSavedLocale(Context context) {
        if (sCachedLocale == null) {
            LocaleUpdater updater = new LocaleUpdater(context);
            String langCode = updater.getUpdatedLocale();
            sCachedLocale = LocaleUpdaterHelper.parseLangCode(langCode);
        }

        return sCachedLocale;
    }

    @SuppressWarnings("deprecation")
    public static void applySavedLocale(Context context) {
        if (context == null) {
            return;
        }

        Locale newLocale = LocaleUpdater.getSavedLocale(context);

        if (newLocale == null) {
            return;
        }

        Resources res = context.getResources();

        if (res == null) {
            return;
        }

        Configuration configuration = res.getConfiguration();

        configuration.locale = newLocale;
        res.updateConfiguration(configuration, res.getDisplayMetrics());
    }

    public static void clearCache() {
        sCachedLocale = null;
    }
}
