package com.liskovsoft.sharedutils.helpers;

import android.app.Activity;
import android.view.KeyEvent;

public class KeyHelpers {
    public static void press(Activity activity, int keyCode) {
        KeyEvent newEventDown = newEvent(KeyEvent.ACTION_DOWN, keyCode);
        KeyEvent newEventUp = newEvent(KeyEvent.ACTION_UP, keyCode);

        activity.dispatchKeyEvent(newEventDown);
        activity.dispatchKeyEvent(newEventUp);
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

    public static KeyEvent newEvent(int action, int keyCode) {
        return new KeyEvent(action, keyCode);
    }
}
