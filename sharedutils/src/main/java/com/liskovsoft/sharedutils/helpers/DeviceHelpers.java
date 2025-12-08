package com.liskovsoft.sharedutils.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings;
import android.util.Range;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.webkit.CookieManager;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;

public final class DeviceHelpers {
    private static final String AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv";
    private static final boolean SAMSUNG = Build.MANUFACTURER.equals("samsung");
    private static final String MIME_VP9 = "video/x-vnd.on2.vp9";
    private static final String MIME_AV1 = "video/av01";
    private static Boolean isTV = null;
    private static Boolean isFireTV = null;
    private static int sMaxHeapMemoryMB = -1;
    private static Boolean sIsVP9Supported;
    private static Boolean sIsAV1Supported;
    private static int sVP9MaxHeight;
    private static int sAV1MaxHeight;
    private static long sCachedRamSize = -1;

    /**
     * <p>The app version code that corresponds to the last update
     * of the media tunneling device blacklist.</p>
     * <p>The value of this variable needs to be updated everytime a new device that does not
     * support media tunneling to match the <strong>upcoming</strong> version code.</p>
     * @see #shouldSupportMediaTunneling()
     */
    public static final int MEDIA_TUNNELING_DEVICE_BLACKLIST_VERSION = 994;

    // region: devices not supporting media tunneling / media tunneling blacklist
    /**
     * <p>Formuler Z8 Pro, Z8, CC, Z Alpha, Z+ Neo.</p>
     * <p>Blacklist reason: black screen</p>
     * <p>Board: HiSilicon Hi3798MV200</p>
     */
    private static final boolean HI3798MV200 = Build.VERSION.SDK_INT == 24
            && Build.DEVICE.equals("Hi3798MV200");
    /**
     * <p>Zephir TS43UHD-2.</p>
     * <p>Blacklist reason: black screen</p>
     */
    private static final boolean CVT_MT5886_EU_1G = Build.VERSION.SDK_INT == 24
            && Build.DEVICE.equals("cvt_mt5886_eu_1g");
    /**
     * Hilife TV.
     * <p>Blacklist reason: black screen</p>
     */
    private static final boolean REALTEKATV = Build.VERSION.SDK_INT == 25
            && Build.DEVICE.equals("RealtekATV");
    /**
     * <p>Phillips 4K (O)LED TV.</p>
     * Supports custom ROMs with different API levels
     */
    private static final boolean PH7M_EU_5596 = Build.VERSION.SDK_INT >= 26
            && Build.DEVICE.equals("PH7M_EU_5596");
    /**
     * <p>Philips QM16XE.</p>
     * <p>Blacklist reason: black screen</p>
     */
    private static final boolean QM16XE_U = Build.VERSION.SDK_INT == 23
            && Build.DEVICE.equals("QM16XE_U");
    /**
     * <p>Sony Bravia VH1.</p>
     * <p>Processor: MT5895</p>
     * <p>Blacklist reason: fullscreen crash / stuttering</p>
     */
    private static final boolean BRAVIA_VH1 = Build.VERSION.SDK_INT == 29
            && Build.DEVICE.equals("BRAVIA_VH1");
    /**
     * <p>Sony Bravia VH2.</p>
     * <p>Blacklist reason: fullscreen crash; this includes model A90J as reported in
     * <a href="https://github.com/TeamNewPipe/NewPipe/issues/9023#issuecomment-1387106242">
     * #9023</a></p>
     */
    private static final boolean BRAVIA_VH2 = Build.VERSION.SDK_INT == 29
            && Build.DEVICE.equals("BRAVIA_VH2");
    /**
     * <p>Sony Bravia Android TV platform 2.</p>
     * Uses a MediaTek MT5891 (MT5596) SoC.
     * @see <a href="https://github.com/CiNcH83/bravia_atv2">
     *     https://github.com/CiNcH83/bravia_atv2</a>
     */
    private static final boolean BRAVIA_ATV2 = Build.DEVICE.equals("BRAVIA_ATV2");
    /**
     * <p>Sony Bravia Android TV platform 3 4K.</p>
     * <p>Uses ARM MT5891 and a {@link #BRAVIA_ATV2} motherboard.</p>
     *
     * @see <a href="https://browser.geekbench.com/v4/cpu/9101105">
     *     https://browser.geekbench.com/v4/cpu/9101105</a>
     */
    private static final boolean BRAVIA_ATV3_4K = Build.DEVICE.equals("BRAVIA_ATV3_4K");
    /**
     * <p>Panasonic 4KTV-JUP.</p>
     * <p>Blacklist reason: fullscreen crash</p>
     */
    private static final boolean TX_50JXW834 = Build.DEVICE.equals("TX_50JXW834");
    /**
     * <p>Bouygtel4K / Bouygues Telecom Bbox 4K.</p>
     * <p>Blacklist reason: black screen; reported at
     * <a href="https://github.com/TeamNewPipe/NewPipe/pull/10122#issuecomment-1638475769">
     *     #10122</a></p>
     */
    private static final boolean HMB9213NW = Build.DEVICE.equals("HMB9213NW");
    // endregion

    private DeviceHelpers() {
    }

    public static boolean isConfirmKey(final int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                return true;
            default:
                return false;
        }
    }

    public static int dpToPx(@Dimension(unit = Dimension.DP) final int dp,
                             @NonNull final Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics());
    }

    public static int spToPx(@Dimension(unit = Dimension.SP) final int sp,
                             @NonNull final Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.getResources().getDisplayMetrics());
    }

    public static boolean isLandscape(final Context context) {
        return context.getResources().getDisplayMetrics().heightPixels < context.getResources()
                .getDisplayMetrics().widthPixels;
    }

    public static boolean hasAnimationsAnimatorDurationEnabled(final Context context) {
        return Settings.System.getFloat(
                context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1F) != 0F;
    }

    /**
     * <p>Some devices have broken tunneled video playback but claim to support it.</p>
     * <p>This can cause a black video player surface while attempting to play a video or
     * crashes while entering or exiting the full screen player.
     * The issue effects Android TVs most commonly.
     * See <a href="https://github.com/TeamNewPipe/NewPipe/issues/5911">#5911</a> and
     * <a href="https://github.com/TeamNewPipe/NewPipe/issues/9023">#9023</a> for more info.</p>
     * @Note Update {@link #MEDIA_TUNNELING_DEVICE_BLACKLIST_VERSION}
     * when adding a new device to the method.
     * @return {@code false} if affected device; {@code true} otherwise
     */
    public static boolean shouldSupportMediaTunneling() {
        // Maintainers note: update MEDIA_TUNNELING_DEVICES_UPDATE_APP_VERSION_CODE
        return !HI3798MV200
                && !CVT_MT5886_EU_1G
                && !REALTEKATV
                && !QM16XE_U
                && !BRAVIA_VH1
                && !BRAVIA_VH2
                && !BRAVIA_ATV2
                && !BRAVIA_ATV3_4K
                && !PH7M_EU_5596
                && !TX_50JXW834
                && !HMB9213NW;
    }

    /**
     * @return whether the device has support for WebView, see
     * <a href="https://stackoverflow.com/a/69626735">https://stackoverflow.com/a/69626735</a>
     */
    public static boolean isWebViewSupported() {
        try {
            CookieManager.getInstance();
            return !isWebViewBroken();
        } catch (final Throwable ignored) {
            return false;
        }
    }

    private static boolean isWebViewBroken() {
        return Build.VERSION.SDK_INT == 19 && isTCL(); // "TCL TV - Harman"
    }

    private static boolean isTCL() {
        return Build.MANUFACTURER.toLowerCase().contains("tcl") || Build.BRAND.toLowerCase().contains("tcl");
    }

    @SuppressWarnings("deprecation")
    public static String getPrimaryAbi() {
        String primaryAbi;

        if (Build.VERSION.SDK_INT >= 21) {
            // For modern devices, use the preferred SUPPORTED_ABIS
            String[] abis = Build.SUPPORTED_ABIS;
            if (abis != null && abis.length > 0) {
                primaryAbi = abis[0];
            } else {
                primaryAbi = Build.CPU_ABI;
            }
        } else {
            primaryAbi = Build.CPU_ABI;
        }

        return primaryAbi;
    }

    public enum ArchType {
        ARM_V7, ARM_64, X86, X86_64, MIPS, UNKNOWN
    }

    public static ArchType getArchType() {
        String abi = getPrimaryAbi();

        if (abi == null) {
            return ArchType.UNKNOWN;
        }

        abi = abi.toLowerCase();

        if (abi.startsWith("armeabi-v7") || abi.startsWith("armeabi")) {
            return ArchType.ARM_V7;
        } else if (abi.startsWith("arm64")) {
            return ArchType.ARM_64;
        } else if (abi.startsWith("x86_64")) {
            return ArchType.X86_64;
        } else if (abi.startsWith("x86")) {
            return ArchType.X86;
        } else if (abi.startsWith("mips")) {
            return ArchType.MIPS;
        } else {
            return ArchType.UNKNOWN;
        }
    }

    public static int getMaxHeapMemoryMB() {
        if (sMaxHeapMemoryMB == -1) {
            long maxMemory = Runtime.getRuntime().maxMemory();
            sMaxHeapMemoryMB = (int)(maxMemory / (1024 * 1024)); // Growth Limit
        }

        return sMaxHeapMemoryMB;
    }

    public static int getAllocatedHeapMemoryMB() {
        long allocatedMemory = Runtime.getRuntime().totalMemory();
        return (int)(allocatedMemory / (1024 * 1024));
    }

    public static boolean isMemoryCritical() {
        return getAllocatedHeapMemoryMB() > getMaxHeapMemoryMB() * 0.5;
    }

    public static boolean isVP9Supported() {
        if (sIsVP9Supported == null) {
            sIsVP9Supported = getCodecMaxHeight(MIME_VP9) != -1;
        }
        return sIsVP9Supported;
    }

    public static boolean isAV1Supported() {
        if (sIsAV1Supported == null) {
            // Not tested yet!!!
            sIsAV1Supported = getCodecMaxHeight(MIME_AV1) != -1;
        }
        return sIsAV1Supported;
    }

    public static boolean isVP9ResolutionSupported(int height) {
        if (height <= 0) {
            return false;
        }

        if (sVP9MaxHeight == 0) { // not initialized
            // TV capabilities sometimes are limited to the screen resolution not real decoder support
            switch (Build.MODEL) {
                // FHD devices with fake 2K support
                case "AFTSSS": // fire tv stick 3th gen
                case "Chromecast HD":
                    sVP9MaxHeight = 1080;
                    break;
                // FHD tvs capable 4K
                case "MiTV-AXSO0":
                case "VIDAA_TV":
                case "PATH_7XPRO":
                    sVP9MaxHeight = 2160;
                    break;
                default:
                    sVP9MaxHeight = getCodecMaxHeight(MIME_VP9);
                    break;
            }
        }

        return height <= sVP9MaxHeight;
    }

    public static boolean isAV1ResolutionSupported(int height) {
        if (height <= 0) {
            return false;
        }

        if (sAV1MaxHeight == 0) { // not initialized
            sAV1MaxHeight = getCodecMaxHeight(MIME_AV1);

            // On Rockchip (and some others) av1 codec info is bugged.
            // Reported max resolution is 360p.
            if (sAV1MaxHeight > 0 && sAV1MaxHeight < 1080) {
                sAV1MaxHeight = 2160;
            }
        }

        return height <= sAV1MaxHeight;
    }

    /**
     * <a href="https://developer.android.com/reference/android/media/MediaCodec">More info</a>
     */
    private static int getCodecMaxHeight(String mimeType) {
        if (VERSION.SDK_INT < 21) {
            return -1;
        }

        try {
            MediaCodecInfo[] codecInfos = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();

            for (MediaCodecInfo codecInfo : codecInfos) {
                if (codecInfo.isEncoder() || !isHardwareAccelerated(codecInfo.getName())) {
                    continue;
                }

                String[] types = codecInfo.getSupportedTypes();

                for (String type : types) {
                    if (type.equalsIgnoreCase(mimeType)) {
                        CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                        Range<Integer> heights = capabilities.getVideoCapabilities().getSupportedHeights();
                        return heights.getUpper();
                    }
                }
            }
        } catch (RuntimeException e) {
            // cannot get MediaCodecList
        }

        return -1;
    }

    /**
     * <a href="https://github.com/google/ExoPlayer/issues/4757">More info</a>
     * @param videoCodecName name from CodecInfo
     * @return is accelerated
     */
    public static boolean isHardwareAccelerated(String videoCodecName) {
        if (videoCodecName == null) {
            return false;
        }

        for (String name : new String[]{"omx.google.", "c2.android."}) {
            if (videoCodecName.toLowerCase().startsWith(name)) {
                return false;
            }
        }

        return true;
    }

    public static long getDeviceRam(Context context) {
        if (sCachedRamSize != -1) {
            return sCachedRamSize;
        }

        if (context == null) {
            return -1;
        }

        long result;

        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
            result = memInfo.totalMem;
        } else {
            result = 500_000_000; // safe value for devices with 1gb or more...
        }

        sCachedRamSize = result;

        return result;
    }
}
