package com.liskovsoft.sharedutils.rx;

import androidx.annotation.Nullable;
import com.liskovsoft.sharedutils.mylogger.Log;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * <a href="https://medium.com/android-news/rxjava-schedulers-what-when-and-how-to-use-it-6cfc27293add">Info about schedulers</a>
 */
public class RxHelper {
    private static final String TAG = RxHelper.class.getSimpleName();

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
        return setup(observable)
                .subscribe(
                        obj -> {}, // ignore result
                        error -> Log.e(TAG, "Execute error: %s", error.getMessage())
                );
    }

    public static <T> Disposable execute(Observable<T> observable, Runnable onFinish) {
        return setup(observable)
                .subscribe(
                        obj -> {}, // ignore result
                        error -> Log.e(TAG, "Execute error: %s", error.getMessage()),
                        onFinish::run
                );
    }

    public static <T> Disposable execute(Observable<T> observable, Runnable onError, Runnable onFinish) {
        return setup(observable)
                .subscribe(
                        obj -> {}, // ignore result
                        error -> onError.run(),
                        onFinish::run
                );
    }

    public static Disposable startInterval(Runnable callback, int periodSec) {
        return interval(periodSec, TimeUnit.SECONDS)
                .subscribe(
                        period -> callback.run(),
                        error -> Log.e(TAG, "startInterval error: %s", error.getMessage())
                );
    }

    public static Disposable runAsync(Runnable callback) {
        return Completable.fromRunnable(callback)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe();
    }

    public static Disposable runAsync(Runnable callback, long delayMs) {
        return Completable.fromRunnable(callback)
                .delaySubscription(delayMs, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe();
    }

    public static Disposable runAsyncUser(Runnable callback) {
        return runAsyncUser(callback, null, null);
    }

    public static Disposable runAsyncUser(Runnable callback, @Nullable OnError onError, @Nullable Runnable onFinish) {
        return Completable.fromRunnable(callback)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            if (onFinish != null) {
                                onFinish.run();
                            }
                        },
                        error -> {
                            if (onError != null) {
                                onError.onError(error);
                            }
                        }
                );
    }

    /**
     * <a href="https://stackoverflow.com/questions/43525052/rxjava2-observable-take-throws-undeliverableexception">More info 1</a>
     * <a href="https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling">More info 2</a>
     */
    public static void setupGlobalErrorHandler() {
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                Log.e(TAG, "Undeliverable exception received, not sure what to do", e.getCause());
                return;
            }
            if ((e instanceof IllegalStateException) &&
                    ((e.getCause() instanceof SocketException) || (e.getCause() instanceof ConnectException))) {
                // network problems (no internet, failed to connect etc)
                Log.e(TAG, "Network error", e.getCause());
                return;
            }
            if ((e instanceof IllegalStateException) && (e.getCause() == null)) {
                Log.e(TAG, "Seems that the user forgot to implement error handler", e);
                return;
            }
            if (e instanceof IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }
        });
    }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return setup(Observable.create(source));
    }

    public static <T> Observable<T> createLong(ObservableOnSubscribe<T> source) {
        return setupLong(Observable.create(source));
    }

    public static <T> Observable<T> fromCallable(Callable<T> supplier) {
        return setup(Observable.fromCallable(supplier));
    }

    public static <T> Observable<T> fromIterable(Iterable<T> source) {
        return setup(Observable.fromIterable(source));
    }

    public static Observable<Void> fromVoidable(Runnable callback) {
        return create(emitter -> {
            callback.run();
            emitter.onComplete();
        });
    }

    public static <T> Observable<T> fromNullable(Callable<T> callback) {
        return create(emitter -> {
            T result = callback.call();

            if (result != null) {
                emitter.onNext(result);
                emitter.onComplete();
            } else {
                // Be aware of OnErrorNotImplementedException exception if error handler not implemented!
                // Essential part to notify about problems. Don't remove!
                onError(emitter, "fromNullable result is null");
                Log.e(TAG, "fromNullable result is null");
            }
        });
    }

    public static Observable<Long> interval(long period, TimeUnit unit) {
        return setupLong(Observable.interval(period, unit));
    }

    /**
     * Fix fall back on the global error handler.
     * <a href="https://stackoverflow.com/questions/44420422/crash-when-sending-exception-through-rxjava">More info</a><br/>
     * Be aware of {@link OnErrorNotImplementedException} exception if error handler not implemented inside subscribe clause!
     */
    public static <T> void onError(ObservableEmitter<T> emitter, String msg) {
        emitter.tryOnError(new IllegalStateException(msg));
    }

    /**
     * Short running tasks <br/>
     * https://stackoverflow.com/questions/33370339/what-is-the-difference-between-schedulers-io-and-schedulers-computation
     */
    private static <T> Observable<T> setup(Observable<T> observable) {
        // NOTE: Schedulers.io() reuses blocked threads in RxJava 2
        // https://github.com/ReactiveX/RxJava/issues/6542
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Long running tasks <br/>
     * https://stackoverflow.com/questions/33370339/what-is-the-difference-between-schedulers-io-and-schedulers-computation
     */
    private static <T> Observable<T> setupLong(Observable<T> observable) {
        // NOTE: Schedulers.io() reuses blocked threads in RxJava 2
        // https://github.com/ReactiveX/RxJava/issues/6542
        // fix blocking (e.g. SponsorBlock not responding)
        return observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
