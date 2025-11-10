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
package com.example.biometricloginsample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.biometricloginsample.FailedLoginFormState
import com.example.biometricloginsample.LoginViewModel
import com.example.biometricloginsample.R
import com.example.biometricloginsample.SuccessfulLoginFormState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    showBiometricButton: Boolean,
    successMessage: String?,
    onUseBiometricsClick: () -> Unit,
    onLoginClick: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val loginFormState by viewModel.loginWithPasswordFormState.observeAsState()
    val loginResult by viewModel.loginResult.observeAsState()
    
    var usernameError: String? by remember { mutableStateOf(null) }
    var passwordError: String? by remember { mutableStateOf(null) }
    var isLoginEnabled by remember { mutableStateOf(false) }
    
    // Update error states based on form state
    loginFormState?.let { formState ->
        when (formState) {
            is SuccessfulLoginFormState -> {
                isLoginEnabled = formState.isDataValid
                usernameError = null
                passwordError = null
            }
            is FailedLoginFormState -> {
                isLoginEnabled = false
                usernameError = formState.usernameError?.let { stringResource(it) }
                passwordError = formState.passwordError?.let { stringResource(it) }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.onLoginDataChanged(username, password)
            },
            label = { Text(stringResource(R.string.username_hint)) },
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.onLoginDataChanged(username, password)
            },
            label = { Text(stringResource(R.string.password)) },
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isLoginEnabled) {
                        onLoginClick(username, password)
                    }
                }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Login button
        Button(
            onClick = { onLoginClick(username, password) },
            enabled = isLoginEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_login))
        }
        
        // Use biometrics button
        if (showBiometricButton) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onUseBiometricsClick) {
                Text(
                    text = stringResource(R.string.btn_biometric_authorization),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Success message
        successMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
