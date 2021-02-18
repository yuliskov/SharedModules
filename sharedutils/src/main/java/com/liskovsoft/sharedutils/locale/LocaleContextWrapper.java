package com.liskovsoft.sharedutils.locale;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.LocaleList;

import java.util.Locale;

/**
 * <a href="https://stackoverflow.com/questions/4985805/set-locale-programmatically">More info</a>
 */
public class LocaleContextWrapper extends ContextWrapper {
    public LocaleContextWrapper(Context base) {
        super(base);
    }

    @SuppressWarnings("deprecation")
    public static Context wrap(Context context, Locale newLocale) {
        if (newLocale == null) {
            return context;
        }

        Resources res = context.getResources();

        if (res == null) {
            return context;
        }

        Configuration configuration = res.getConfiguration();

        if (VERSION.SDK_INT >= 24) {
            configuration.setLocale(newLocale);

            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);

            context = context.createConfigurationContext(configuration);

        } else if (VERSION.SDK_INT >= 17) {
            configuration.setLocale(newLocale);
            context = context.createConfigurationContext(configuration);

        } else {
            configuration.locale = newLocale;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }

        return new ContextWrapper(context);
    }

    @SuppressWarnings("deprecation")
    public static void apply(Configuration configuration, Locale newLocale) {
        if (newLocale == null || configuration == null) {
            return;
        }

        if (VERSION.SDK_INT >= 24) {
            configuration.setLocale(newLocale);
            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
        } else if (VERSION.SDK_INT >= 17) {
            configuration.setLocale(newLocale);
        } else {
            configuration.locale = newLocale;
        }
    }
}
