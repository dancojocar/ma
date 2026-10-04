package com.unieats.app.cloud

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashConsent @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("privacy", Context.MODE_PRIVATE)

    private val _granted = MutableStateFlow(prefs.getBoolean(KEY, false))
    val granted: StateFlow<Boolean> = _granted.asStateFlow()

    val isGranted: Boolean get() = _granted.value

    fun set(granted: Boolean) {
        prefs.edit { putBoolean(KEY, granted) }
        _granted.value = granted
    }

    private companion object {
        const val KEY = "crash_reporting_consent"
    }
}
