package com.liskovsoft.sharedutils.helpers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Helpers {
    private static HashMap<String, List<String>> sCache = new HashMap<>();
    public static final int REMOVE_PACKAGE_CODE = 521;

    /**
     * Simple wildcard matching routine. Implemented without regex. So you may expect huge performance boost.
     * @param host
     * @param mask
     * @return
     */
    public static boolean matchSubstr(String host, String mask) {
        String[] sections = mask.split("\\*");
        String text = host;
        for (String section : sections) {
            int index = text.indexOf(section);
            if (index == -1) {
                return false;
            }
            text = text.substring(index + section.length());
        }
        return true;
    }

    public static boolean matchSubstrNoCase(String host, String mask) {
        return matchSubstr(host.toLowerCase(), mask.toLowerCase());
    }

    public static InputStream appendStream(InputStream first, InputStream second) {
        return FileHelpers.appendStream(first, second);
    }

    public static String encodeURI(byte[] data) {
        try {
            // make behaviour of java uri-encode the same as javascript's one
            return URLEncoder.encode(new String(data, "UTF-8"), "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getDeviceName() {
        return String.format("%s (%s)", Build.MODEL, Build.PRODUCT);
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release +")";
    }

    public static boolean isGenymotion() {
        String deviceName = getDeviceName();

        return deviceName.contains("(vbox86p)");
    }

    public static boolean deviceMatch(String[] devicesToProcess) {
        String thisDeviceName = Helpers.getDeviceName();
        for (String deviceName : devicesToProcess) {
            boolean match = matchSubstrNoCase(thisDeviceName, deviceName);
            if (match) {
                return true;
            }
        }
        return false;
    }

    public static String toString(Throwable ex) {
        if (ex instanceof IllegalStateException &&
                ex.getCause() != null) {
            ex = ex.getCause();
        }
        return String.format("%s: %s", ex.getClass().getCanonicalName(), ex.getMessage());
    }

    public static String toString(InputStream content) {
        return FileHelpers.toString(content);
    }

    public static InputStream toStream(String content) {
        return FileHelpers.toStream(content);
    }

    public static void postOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static String unixToLocalDate(Context ctx, String timestamp) {
        Locale current = ctx.getResources().getConfiguration().locale;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, current);
        Date date;
        if (timestamp == null) {
            date = new Date();
        } else {
            date = new Date((long) Integer.parseInt(timestamp) * 1000);
        }
        return dateFormat.format(date);
    }

    public static String runMultiMatcher(String input, String... patterns) {
        if (input == null) {
            return null;
        }

        Pattern regex;
        Matcher matcher;
        String result = null;
        for (String pattern : patterns) {
            regex = Pattern.compile(pattern);
            matcher = regex.matcher(input);

            if (matcher.find()) {
                result = matcher.group(matcher.groupCount()); // get last group
                break;
            }
        }

        return result;
    }

    public static boolean isCallable(Context ctx, Intent intent) {
        List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Format float and remove unneeded zeroes after dot
     */
    public static String formatFloat(double d) {
        // This is to show symbol . instead of ,
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        // Define the maximum number of decimals (number of symbols #)
        DecimalFormat df = new DecimalFormat("#.##", otherSymbols);

        return df.format(d);
    }

    /**
     * Limit digits after dot
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * Get scale for use in {@link android.webkit.WebView} instantiation
     * @param ctx context
     * @param picWidth constant that I knew beforehand
     * @return calculated scale
     */
    public static int getScale(Context ctx, int picWidth) {
        Point p = new Point();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getSize(p);
        int width = p.x;
        Double val = (double) width / (double) picWidth;
        val = val * 100d;
        return val.intValue();
    }

    private static boolean isNaN(String num) {
        return num == null                                      ||
               num.isEmpty()                                    ||
               num.contains(" ")                                ||
               num.contains(";")                                ||
               num.contains("&")                                ||
               num.contains(",")                                ||
               num.contains(".")                                ||
               num.contains(":")                                ||
               num.contains("/")                                ||
               num.contains("\\")                               ||
               !Character.isDigit(num.charAt(num.length() - 1)) ||
               !Character.isDigit(num.charAt(0));
    }

    public static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    /**
     * Force normal font size regardless of the system settings
     * @param configuration app config
     * @param ctx activity
     */
    public static void adjustFontScale(Configuration configuration, Activity ctx) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return;
        }

        configuration.fontScale = (float) 1.0; // normal size
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        ctx.getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    public static <T> Map<String, T> convertToObj(String jsonContent) {
        Type type = new TypeToken<Map<String, T>>(){}.getType();
        Gson gson = new Gson();
        return gson.fromJson(jsonContent, type);
    }

    public static String toIntString(Object floatOrIntString) {
        if (floatOrIntString == null) {
            return null;
        }

        float val = Float.parseFloat(String.valueOf(floatOrIntString));
        return String.valueOf((int) val);
    }

    /**
     * Return true to first matched string from the array
     * @param fullStr full string
     * @param nameArr array to match
     * @return whether ended with arr
     */
    public static boolean endsWith(String fullStr, String[] nameArr) {
        for (String name : nameArr) {
            if (fullStr.endsWith(name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMicAvailable(Context context) {
        PackageManager pm = context.getPackageManager();

        boolean isMicAvail = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);

        //boolean isLeanback = false;
        //
        //if (VERSION.SDK_INT >= 21) {
        //    // Android TV user likely have mics
        //    isLeanback = isAndroidTV(context) || isAmazonFireTVDevice();
        //}

        return isMicAvail || VERSION.SDK_INT >= 21;
    }

    public static boolean isAndroidTVLauncher(Context context) {
        return isPackageExists(context, "com.google.android.leanbacklauncher");
    }

    public static boolean isAndroidTVRecommendations(Context context) {
        return isPackageExists(context, "com.google.android.leanbacklauncher.recommendations");
    }

    public static boolean isATVChannelsSupported(Context context) {
        return VERSION.SDK_INT >= 26 && isAndroidTVLauncher(context);
    }

    public static boolean isAndroidTV(Context context) {
        PackageManager pm = context.getPackageManager();

        if (VERSION.SDK_INT <= 21) {
            return false;
        }

        return (pm.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
                || pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK));
    }

    public static boolean matchAll(String input, String... regex) {
        for (String reg : regex) {
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(input);
            if (!matcher.find()) {
                return false;
            }
        }

        return true;
    }

    public static void makeActivityFullscreen(Activity activity) {
        activity.getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);

        if (VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public static void makeActivityHorizontal(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public static boolean equals(String first, String second) {
        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.equals(second);
    }

    public static boolean isDash(String id) {
        if (!Helpers.isNumeric(id)) {
            return false;
        }

        int maxRegularITag = 50;
        int itag = Integer.parseInt(id);

        return itag > maxRegularITag;
    }

    public static BufferedReader exec(String... params) throws IOException {
        Process process = Runtime.getRuntime().exec(params);
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    public static void enableButtonSounds(Context context, boolean enable) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, !enable);
    }

    /**
     * Find all packages starting for specified name
     * @param context ctx
     * @param pkgPrefix starts with
     * @return packages or empty list if not found
     */
    public static List<String> findPackagesByPrefix(Context context, String pkgPrefix) {
        List<String> pkgNames = new ArrayList<>();

        for (ApplicationInfo info : getInstalledPackages(context)) {
            if (info.packageName != null && info.packageName.startsWith(pkgPrefix)) {
                pkgNames.add(info.packageName);
            }
        }

        return pkgNames;
    }

    public static boolean isPackageExists(Context context, String pkgName) {
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo = null;

        try {
            packageInfo = manager.getPackageInfo(pkgName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            // NOP
        }

        return packageInfo != null;
    }

    public static void removePackage(Context context, String pkgName) {
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + pkgName));
        context.startActivity(intent);
    }

    public static void removePackageAndGetResult(Activity context, String pkgName) {
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + pkgName));
        context.startActivityForResult(intent, REMOVE_PACKAGE_CODE);
    }

    // NOTE: as of Oreo you must also add the REQUEST_INSTALL_PACKAGES permission to your manifest. Otherwise it just silently fails
    public static void installPackage(Context context, String packagePath) {
        if (packagePath == null || context == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri file = FileHelpers.getFileUri(context, packagePath);
        intent.setDataAndType(file, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION); // without this flag android returned a intent error!
        context.getApplicationContext().startActivity(intent);
    }

    public static List<ApplicationInfo> getInstalledPackages(Context context) {
        final PackageManager pm = context.getPackageManager();
        //get a list of installed apps.

        return pm.getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public static List<String> getInstalledPackagesWithMainActivity(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        List<String> result = new ArrayList<>();
        for (ResolveInfo info : pkgAppsList) {
            result.add(info.activityInfo.packageName);
        }

        return result;
    }

    public static boolean isEmpty(Intent intent) {
        if (intent == null) {
            return true;
        }

        if (intent.getExtras() == null) {
            return true;
        }

        return intent.getExtras().isEmpty();
    }

    public static void mergeIntents(Intent mainIntent, Intent newIntent) {
        Bundle extras = mainIntent.getExtras();

        if (extras != null) {
            extras.putAll(newIntent.getExtras());
            mainIntent.putExtras(extras);
        }
    }

    public static void bringToBackOld(View child) {
        ViewGroup parent = (ViewGroup) child.getParent();
        if (parent != null && parent.indexOfChild(child) != 0) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    /**
     * Not working!!!
     */
    public static void bringToBack(View myCurrentView) {
        ViewGroup myViewGroup = ((ViewGroup) myCurrentView.getParent());
        int index = myViewGroup.indexOfChild(myCurrentView);
        for(int i = 0; i < index; i++) {
            myViewGroup.bringChildToFront(myViewGroup.getChildAt(i));
        }
    }

    public static KeyEvent newEvent(KeyEvent origin, int newKeyCode) {
        return new KeyEvent(
                origin.getDownTime(),
                origin.getEventTime(),
                origin.getAction(),
                newKeyCode,
                origin.getRepeatCount(),
                origin.getMetaState(),
                origin.getDeviceId(),
                origin.getScanCode(),
                origin.getFlags(),
                origin.getSource());
    }

    public static void enableScreensaver(Activity context) {
        if (context == null) {
            return;
        }

        context.runOnUiThread(() -> context.getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON));
    }

    public static void disableScreensaver(Activity context) {
        if (context == null) {
            return;
        }

        context.runOnUiThread(() -> context.getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON));
    }

    /**
     * Utility method to check if device is Amazon Fire TV device
     * @return {@code true} true if device is Amazon Fire TV device.
     */
    public static boolean isAmazonFireTVDevice() {
        String deviceName = Build.MODEL;
        String manufacturerName = Build.MANUFACTURER;
        return (deviceName.startsWith("AFT")
                && "Amazon".equalsIgnoreCase(manufacturerName));
    }

    public static boolean isActivityExists(Intent intent, Context context) {
        if (intent == null || context == null) {
            return false;
        }

        return intent.resolveActivityInfo(context.getPackageManager(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) != null;
    }

    /**
     * Get a MemoryInfo object for the device's current memory status.
     */
    public static ActivityManager.MemoryInfo getAvailableMemory(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    /**
     * Check that the app could be easily uninstalled without root.
     */
    public static boolean isUserApp(PackageInfo info) {
        if (info != null && info.applicationInfo != null) {
            ApplicationInfo ai = info.applicationInfo;
            int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
            return (ai.flags & mask) == 0;
        }

        return false;
    }

    public static boolean nearlyEqual(float a, float b, float epsilon) {
        final float diff = Math.abs(a - b);

        return diff <= epsilon;
    }

    public static boolean floatEquals(float num1, float num2) {
        float epsilon = 0.1f;
        return Math.abs(num1 - num2) < epsilon;
    }

    public static String getSimpleClassName(String name) {
        if (name == null) {
            return null;
        }

        return name.substring(name.lastIndexOf('.') + 1);
    }
}
