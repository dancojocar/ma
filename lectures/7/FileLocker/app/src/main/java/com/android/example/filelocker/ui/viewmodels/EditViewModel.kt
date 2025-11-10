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
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.android.example.filelocker.loge
import com.android.example.filelocker.urlEncode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class EditViewModel(
    private val context: Context,
    private val existingFileTitle: String
) : ViewModel() {
    
    private val _title = MutableStateFlow(existingFileTitle)
    val title: StateFlow<String> = _title
    
    private val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body
    
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> = _snackbarMessage
    
    init {
        if (existingFileTitle.isNotBlank()) {
            loadFile()
        }
    }
    
    private fun loadFile() {
        try {
            val content = decryptFile(existingFileTitle)
            _body.value = content
        } catch (e: Exception) {
            loge("Unable to decrypt file", e)
            showMessage("Unable to decrypt file")
        }
    }
    
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }
    
    fun updateBody(newBody: String) {
        _body.value = newBody
    }
    
    fun saveFile() {
        val currentTitle = _title.value
        val currentBody = _body.value
        
        if (currentTitle.isBlank()) return
        
        try {
            deleteFile(existingFileTitle)
            val encryptedFile = getEncryptedFile(currentTitle)
            encryptedFile.openFileOutput().use { output ->
                output.write(currentBody.toByteArray())
            }
        } catch (e: Exception) {
            loge("Unable to save file", e)
            showMessage("Unable to save file")
        }
    }
    
    fun deleteFile() {
        deleteFile(existingFileTitle)
    }
    
    private fun decryptFile(title: String): String {
        val encryptedFile = getEncryptedFile(title)
        
        try {
            encryptedFile.openFileInput().use { input ->
                return String(input.readBytes(), Charsets.UTF_8)
            }
        } catch (e: Exception) {
            loge("Unable to decrypt", e)
            showMessage("Unable to decrypt")
            return ""
        }
    }
    
    private fun deleteFile(title: String) {
        if (title.isBlank()) return
        val file = File(context.filesDir, title.urlEncode())
        if (file.exists()) file.delete()
    }
    
    private fun getEncryptedFile(name: String): EncryptedFile {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedFile.Builder(
            context,
            File(context.filesDir, name.urlEncode()),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }
    
    private fun showMessage(message: String) {
        _snackbarMessage.value = message
    }
    
    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}

class EditViewModelFactory(
    private val context: Context,
    private val fileTitle: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            return EditViewModel(context, fileTitle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
