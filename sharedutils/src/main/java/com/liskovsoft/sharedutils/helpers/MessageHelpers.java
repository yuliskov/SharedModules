package com.liskovsoft.sharedutils.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.liskovsoft.sharedutils.R;

import java.util.ArrayList;
import java.util.List;

public class MessageHelpers {
    private static long sExitMsgTimeMS = 0;
    private static final int LONG_MSG_TIMEOUT = 5000;
    private static final List<Toast> mToasts = new ArrayList<>();
    private static final Runnable mCleanupContext = mToasts::clear;
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
        if (ctx == null) {
            return;
        }

        Runnable toast = () -> {
            try {
                Toast currentToast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
                fixTextSize(currentToast, ctx);
                saveToast(currentToast);
                currentToast.show();

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
            showMessage(ctx, msg);
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
        float textSize = context.getResources().getDimension(R.dimen.dialog_text_size);

        TextView messageTextView = extractMessageView(toast);
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    private static void saveToast(Toast newToast) {
        mToasts.add(newToast);
        CharSequence originText = extractText(newToast);

        for (Toast toast : mToasts) {
            // Smart cancel only toasts that have different message
            // So remains possibility to long message to be displayed
            if (!extractText(toast).equals(originText)) {
                toast.cancel();
            }
        }
    }

    private static CharSequence extractText(Toast toast) {
        TextView messageTextView = extractMessageView(toast);
        return messageTextView.getText();
    }

    private static TextView extractMessageView(Toast toast) {
        ViewGroup group = (ViewGroup) toast.getView();
        return (TextView) group.getChildAt(0);
    }

    private static void setupCleanup() {
        mHandler.removeCallbacks(mCleanupContext);
        mHandler.postDelayed(mCleanupContext, 10_000);
    }
}
