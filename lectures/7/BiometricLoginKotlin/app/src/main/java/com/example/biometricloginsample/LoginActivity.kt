/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.biometricloginsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.biometricloginsample.ui.LoginScreen
import com.example.biometricloginsample.ui.theme.BiometricLoginTheme

/**
 * 1) after entering "valid" username and password, login button becomes enabled
 * 2) User clicks biometrics?
 *   - a) if no template exists, then ask user to register template
 *   - b) if template exists, ask user to confirm by entering username & password
 */
class LoginActivity : FragmentActivity() {
  private val TAG = "LoginActivity"
  private lateinit var biometricPrompt: BiometricPrompt
  private val cryptographyManager = CryptographyManager()
  private val ciphertextWrapper
    get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
      applicationContext,
      SHARED_PREFS_FILENAME,
      Context.MODE_PRIVATE,
      CIPHERTEXT_WRAPPER
    )
  private val loginWithPasswordViewModel by viewModels<LoginViewModel>()
  private var successMessage by mutableStateOf<String?>(null)
  private var showBiometricButton by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val canAuthenticate = BiometricManager.from(applicationContext)
      .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    Log.d(TAG, "canAuthenticate: $canAuthenticate")
    showBiometricButton = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    
    setContent {
      BiometricLoginTheme {
        LoginScreen(
          viewModel = loginWithPasswordViewModel,
          showBiometricButton = showBiometricButton,
          successMessage = successMessage,
          onUseBiometricsClick = {
            if (ciphertextWrapper != null) {
              showBiometricPromptForDecryption()
            } else {
              startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
            }
          },
          onLoginClick = { username, password ->
            loginWithPasswordViewModel.login(username, password)
          }
        )
      }
    }
  }

  /**
   * The logic is kept inside onResume instead of onCreate so that authorizing biometrics takes
   * immediate effect.
   */
  override fun onResume() {
    super.onResume()

    if (ciphertextWrapper != null) {
      if (SampleAppUser.fakeToken == null) {
        showBiometricPromptForDecryption()
      } else {
        // The user has already logged in, so proceed to the rest of the app
        // this is a todo for you, the developer
        successMessage = getString(R.string.already_signedin)
      }
    }
  }

  // BIOMETRICS SECTION

  private fun showBiometricPromptForDecryption() {
    ciphertextWrapper?.let { textWrapper ->
      val secretKeyName = getString(R.string.secret_key_name)
      val cipher = cryptographyManager.getInitializedCipherForDecryption(
        secretKeyName, textWrapper.initializationVector
      )
      biometricPrompt =
        BiometricPromptUtils.createBiometricPrompt(
          this,
          ::decryptServerTokenFromStorage
        )
      val promptInfo = BiometricPromptUtils.createPromptInfo(this)
      biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
  }

  private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
    ciphertextWrapper?.let { textWrapper ->
      authResult.cryptoObject?.cipher?.let {
        val plaintext =
          cryptographyManager.decryptData(textWrapper.ciphertext, it)
        SampleAppUser.fakeToken = plaintext
        // Now that you have the token, you can query server for everything else
        // the only reason we call this fakeToken is because we didn't really get it from
        // the server. In your case, you will have gotten it from the server the first time
        // and therefore, it's a real token.

        successMessage = getString(R.string.already_signedin)
      }
    }
  }

}