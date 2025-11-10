/*
 * Copyright (C) 2020 The Android Open Source Project
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
 * limitations under the License
 */
package com.example.biometricloginsample

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Observer
import com.example.biometricloginsample.ui.EnableBiometricLoginScreen
import com.example.biometricloginsample.ui.theme.BiometricLoginTheme
import logd

class EnableBiometricLoginActivity : FragmentActivity() {
  private lateinit var cryptographyManager: CryptographyManager
  private val loginViewModel by viewModels<LoginViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    loginViewModel.loginResult.observe(this, Observer {
      val loginResult = it ?: return@Observer
      if (loginResult.success) {
        showBiometricPromptForEncryption()
      }
    })
    
    setContent {
      BiometricLoginTheme {
        EnableBiometricLoginScreen(
          viewModel = loginViewModel,
          onCancelClick = { finish() },
          onAuthorizeClick = { username, password ->
            loginViewModel.login(username, password)
          }
        )
      }
    }
  }

  private fun showBiometricPromptForEncryption() {
    val canAuthenticate =
      BiometricManager.from(applicationContext).canAuthenticate(BIOMETRIC_STRONG)
    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
      val secretKeyName = getString(R.string.secret_key_name)
      cryptographyManager = CryptographyManager()
      val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
      val biometricPrompt =
        BiometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
      val promptInfo = BiometricPromptUtils.createPromptInfo(this)
      biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
  }

  private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
    authResult.cryptoObject?.cipher?.apply {
      SampleAppUser.fakeToken?.let { token ->
        logd("The token from server is $token")
        val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
        cryptographyManager.persistCiphertextWrapperToSharedPrefs(
          encryptedServerTokenWrapper,
          applicationContext,
          SHARED_PREFS_FILENAME,
          Context.MODE_PRIVATE,
          CIPHERTEXT_WRAPPER
        )
      }
    }
    finish()
  }
}