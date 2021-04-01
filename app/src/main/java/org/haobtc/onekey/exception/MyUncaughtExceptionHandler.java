package org.haobtc.onekey.exception;

import androidx.annotation.NonNull;

import io.sentry.Sentry;

/**
 * @author liyan
 * @date 2020/8/31
 */

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Sentry.captureException(e);
    }
}
