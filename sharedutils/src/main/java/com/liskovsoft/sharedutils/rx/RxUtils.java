package com.liskovsoft.sharedutils.rx;

import com.liskovsoft.sharedutils.mylogger.Log;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <a href="https://medium.com/android-news/rxjava-schedulers-what-when-and-how-to-use-it-6cfc27293add">Info about schedulers</a>
 */
public class RxUtils {
    private static final String TAG = RxUtils.class.getSimpleName();

    public static void disposeActions(Disposable... actions) {
        if (actions != null) {
            for (Disposable action : actions) {
                if (isActionRunning(action)) {
                    action.dispose();
                }
            }
        }
    }

    public static void disposeActions(List<Disposable> actions) {
        if (actions != null) {
            for (Disposable action : actions) {
                if (isActionRunning(action)) {
                    action.dispose();
                }
            }
            actions.clear();
        }
    }

    /**
     * NOTE: Don't use it to check that action in completed inside other action (scrollEnd bug).
     */
    public static boolean isAnyActionRunning(Disposable... actions) {
        if (actions != null) {
            for (Disposable action : actions) {
                if (isActionRunning(action)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * NOTE: Don't use it to check that action in completed inside other action (scrollEnd bug).
     */
    public static boolean isAnyActionRunning(List<Disposable> actions) {
        if (actions != null) {
            for (Disposable action : actions) {
                if (isActionRunning(action)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isActionRunning(Disposable action) {
        return action != null && !action.isDisposed();
    }

    public static <T> Disposable execute(Observable<T> observable) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        obj -> {}, // ignore result
                        error -> Log.e(TAG, "Execute error: %s", error.getMessage())
                );
    }

    public static <T> Disposable execute(Observable<T> observable, Runnable onFinish) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        obj -> {}, // ignore result
                        error -> Log.e(TAG, "Execute error: %s", error.getMessage()),
                        onFinish::run
                );
    }

    public static <T> Disposable execute(Observable<T> observable, Runnable onError, Runnable onFinish) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        obj -> {}, // ignore result
                        error -> onError.run(),
                        onFinish::run
                );
    }

    public static Disposable startInterval(Runnable callback, int periodSec) {
        Observable<Long> playbackProgressObservable =
                Observable.interval(periodSec, TimeUnit.SECONDS);

        return playbackProgressObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        period -> callback.run(),
                        error -> Log.e(TAG, "startInterval error: %s", error.getMessage())
                );
    }

    public static Disposable runAsync(Runnable callback) {
        return Completable.fromAction(callback::run)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe();
    }

    public static Disposable runAsync(Runnable callback, long delayMs) {
        return Completable.fromAction(callback::run)
                .delaySubscription(delayMs, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe();
    }
}
