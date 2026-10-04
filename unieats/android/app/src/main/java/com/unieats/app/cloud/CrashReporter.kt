package com.unieats.app.cloud

import android.util.Log

/**
 * Where errors go. The app depends on this interface only; swapping [LogcatCrashReporter] for a
 * Crashlytics or Sentry implementation is a one-line change in CloudModule.
 */
interface CrashReporter {
    fun recordError(throwable: Throwable)
    fun log(message: String)
}

class LogcatCrashReporter : CrashReporter {
    override fun recordError(throwable: Throwable) {
        Log.e(TAG, "recorded error", throwable)
    }

    override fun log(message: String) {
        Log.i(TAG, message)
    }

    private companion object {
        const val TAG = "CrashReporter"
    }
}

/** Nothing leaves the device unless the user opted in (GDPR-style consent, off by default). */
class ConsentGatedCrashReporter(
    private val delegate: CrashReporter,
    private val consent: CrashConsent
) : CrashReporter {
    override fun recordError(throwable: Throwable) {
        if (consent.isGranted) delegate.recordError(throwable)
    }

    override fun log(message: String) {
        if (consent.isGranted) delegate.log(message)
    }
}
