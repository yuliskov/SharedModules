package com.liskovsoft.sharedutils.locale;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * <a href="https://stackoverflow.com/questions/4985805/set-locale-programmatically">More info</a>
 */
public class LocaleContextWrapper extends ContextWrapper {
    public LocaleContextWrapper(Context base) {
        super(base);
    }

    public static Context wrap(Context context, Locale newLocale) {
        return wrap(context, newLocale, null);
    }
    
    public static Context wrap(Context context, Locale newLocale, DisplayMetrics customMetrics) {
        if (newLocale == null) {
            return context;
        }

        Resources res = context.getResources();

        if (res == null) {
            return context;
        }

        if (VERSION.SDK_INT >= 17) {
            Configuration override = createOverride(res.getConfiguration(), newLocale, customMetrics);

            if (VERSION.SDK_INT >= 24) {
                LocaleList.setDefault(new LocaleList(newLocale));
            }

            context = context.createConfigurationContext(override);
        } else {
            Configuration configuration = res.getConfiguration();

            if (customMetrics != null) {
                configuration.densityDpi = (int) (customMetrics.density * 160); // 160 is the baseline DPI
            }

            configuration.locale = newLocale;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }

        return new ContextWrapper(context);
    }

    /**
     * Override for {@link Context#createConfigurationContext}: only the fields that should differ
     * from the base context go in.<br/>
     * The platform treats every field set in an override Configuration as explicit and re-applies
     * it on top of each new activity configuration. Passing a copy of the whole current
     * Configuration (as this class used to) therefore pinned the orientation, screen size and
     * window bounds to their values at creation time: after a rotation
     * {@code getResources().getConfiguration().orientation} kept the old value, {@code -land}
     * resources never loaded and {@code WindowManager.getCurrentWindowMetrics()} reported stale
     * bounds. Everything not set here keeps following the window.
     */
    static Configuration createOverride(Configuration base, Locale newLocale, DisplayMetrics customMetrics) {
        Configuration override = new Configuration();
        // new Configuration() defaults fontScale to 1, which would replace the user's font size.
        override.fontScale = base.fontScale;
        // A parent context may have forced the night mode; nested overrides don't stack, so keep it.
        override.uiMode = base.uiMode;
        // Keep the size/long bits; setLocale() below only rewrites the layout direction.
        override.screenLayout = base.screenLayout;
        override.setLocale(newLocale);

        if (customMetrics != null) {
            override.densityDpi = (int) (customMetrics.density * 160); // 160 is the baseline DPI
        }

        return override;
    }

    public static void applySavedLocale(Context context, Locale newLocale, DisplayMetrics customMetrics) {
        if (context == null) {
            return;
        }

        if (newLocale == null) {
            return;
        }

        Resources res = context.getResources();

        if (res == null) {
            return;
        }

        Configuration configuration = res.getConfiguration();

        if (customMetrics != null) {
            configuration.densityDpi = (int) (customMetrics.density * 160); // 160 is the baseline DPI
        }

        configuration.locale = newLocale;
        res.updateConfiguration(configuration, res.getDisplayMetrics());
    }
}
