package com.unieats.app.ui.auth

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.crypto.Cipher
import kotlin.coroutines.resume

/** API 30+ (one key spec for every device) with a strong (class 3) biometric enrolled. */
fun Context.canUseBiometricUnlock(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

/** Re-checked on every resume: the user may have enrolled a fingerprint in Settings meanwhile. */
@Composable
fun rememberBiometricUnlockAvailable(): Boolean {
    val context = LocalContext.current
    var available by remember { mutableStateOf(context.canUseBiometricUnlock()) }
    LifecycleResumeEffect(context) {
        available = context.canUseBiometricUnlock()
        onPauseOrDispose { }
    }
    return available
}

/**
 * Shows the system biometric sheet for [cipher]. Returns the cipher the Keystore has now
 * unlocked for one use, or null if the user pressed [negativeButton], cancelled, or ran out of
 * attempts.
 */
suspend fun FragmentActivity.authenticate(cipher: Cipher, title: String, negativeButton: String): Cipher? =
    suspendCancellableCoroutine { continuation ->
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    continuation.resume(result.cryptoObject?.cipher)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resume(null)
                }

                // onAuthenticationFailed is one unrecognised finger: the sheet stays up for another try.
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeButton)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
        prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
        continuation.invokeOnCancellation { prompt.cancelAuthentication() }
    }
