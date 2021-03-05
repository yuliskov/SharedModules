package com.liskovsoft.sharedutils.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.liskovsoft.sharedutils.R;

public class MessageHelpers {
    private static long sExitMsgTimeMS = 0;
    private static final int LONG_MSG_TIMEOUT = 5000;
    private static float mTextSize;
    private static Toast mCurrentToast;
    private static final Runnable mCleanupContext = () -> mCurrentToast = null;
    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    public static void showMessage(final Context ctx, final String TAG, final Throwable ex) {
        showMessage(ctx, TAG, Helpers.toString(ex));
    }

    public static void showMessage(final Context ctx, final String TAG, final String msg) {
        showMessage(ctx, String.format("%s: %s", TAG, msg));
    }

    public static void showMessageThrottled(final Context ctx, final String msg) {
        // throttle msg calls
        if (System.currentTimeMillis() - sExitMsgTimeMS < LONG_MSG_TIMEOUT) {
            return;
        }
        sExitMsgTimeMS = System.currentTimeMillis();
        showMessage(ctx, msg);
    }

    public static void showMessageThrottled(final Context ctx, final int msgResId) {
        showMessageThrottled(ctx, ctx.getString(msgResId));
    }

    public static void showMessage(final Context ctx, final String msg) {
        showMessage(ctx, msg, true);
    }

    private static void showMessage(final Context ctx, final String msg, boolean cancelPrevious) {
        if (ctx == null) {
            return;
        }

        Runnable toast = () -> {
            try {
                if (mCurrentToast != null && cancelPrevious) {
                    mCurrentToast.cancel();
                }

                mCurrentToast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
                fixTextSize(mCurrentToast, ctx);
                mCurrentToast.show();

                setupCleanup();
            } catch (Exception ex) { // NPE fix
                ex.printStackTrace();
            }
        };

        if (Looper.myLooper() == Looper.getMainLooper()) {
            toast.run();
        } else {
            new Handler(Looper.getMainLooper()).post(toast);
        }
    }

    /**
     * Shows toast message.<br/>
     * Uses resource id as message.
     * @param ctx context
     * @param resId resource id
     */
    public static void showMessage(Context ctx, int resId) {
        if (ctx != null) {
            showMessage(ctx, ctx.getResources().getString(resId));
        }
    }

    /**
     * Shows formatted toast message.<br/>
     * Uses resource id as message.
     * @param ctx context
     * @param resId resource id
     * @param formatArgs format arguments
     */
    public static void showMessage(Context ctx, int resId, Object... formatArgs) {
        if (ctx != null) {
            showMessage(ctx, ctx.getResources().getString(resId, formatArgs));
        }
    }

    /**
     * Shows long toast message.<br/>
     * Uses resource id as message.
     * @param ctx context
     * @param resId resource id
     */
    public static void showLongMessage(Context ctx, int resId) {
        if (ctx != null) {
            showLongMessage(ctx, ctx.getResources().getString(resId));
        }
    }

    public static void showLongMessage(Context ctx, String msg) {
        for (int i = 0; i < 3; i++) {
            showMessage(ctx, msg, false);
        }
    }

    public static void showLongMessage(Context ctx, String TAG, String msg) {
        for (int i = 0; i < 3; i++) {
            showMessage(ctx, TAG, msg);
        }
    }

    public static void showLongMessageEndPause(Context context, int resId) {
        showLongMessage(context, resId);

        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void fixTextSize(Toast toast, Context context) {
        if (mTextSize == 0) {
            mTextSize = context.getResources().getDimension(R.dimen.dialog_text_size);
        }

        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
    }

    private static void setupCleanup() {
        mHandler.removeCallbacks(mCleanupContext);
        mHandler.postDelayed(mCleanupContext, 5_000);
    }
}
