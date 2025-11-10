/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.filelocker.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.example.filelocker.DirectoryLiveData
import com.android.example.filelocker.FileEntity

private const val ENCRYPTED_PREFS_FILE_NAME = "default_prefs"
private const val ENCRYPTED_PREFS_PASSWORD_KEY = "key_prefs_password"

class FileLockerViewModel(private val context: Context) : ViewModel() {
    
    private val sharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    val files: LiveData<List<FileEntity>> = DirectoryLiveData(context.filesDir)
    
    private val _hasPassword = MutableLiveData<Boolean>()
    val hasPassword: LiveData<Boolean> = _hasPassword
    
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> = _snackbarMessage
    
    init {
        updatePasswordStatus()
    }
    
    private fun updatePasswordStatus() {
        _hasPassword.value = getPassword() != null
    }
    
    private fun getPassword(): String? {
        return sharedPreferences.getString(ENCRYPTED_PREFS_PASSWORD_KEY, null)
    }
    
    fun verifyPassword(password: String): Boolean {
        return password == getPassword()
    }
    
    fun setPassword(currentPassword: String?, newPassword: String?) {
        if (currentPassword != getPassword()) {
            showMessage("Current password is incorrect")
            return
        }
        
        if (newPassword.isNullOrBlank()) {
            sharedPreferences.edit().putString(ENCRYPTED_PREFS_PASSWORD_KEY, null).apply()
            showMessage("Password cleared")
        } else {
            sharedPreferences.edit().putString(ENCRYPTED_PREFS_PASSWORD_KEY, newPassword).apply()
            showMessage("Password set")
        }
        updatePasswordStatus()
    }
    
    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }
    
    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}

class FileLockerViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileLockerViewModel::class.java)) {
            return FileLockerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
