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
package com.example.biometricloginsample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.biometricloginsample.FailedLoginFormState
import com.example.biometricloginsample.LoginViewModel
import com.example.biometricloginsample.R
import com.example.biometricloginsample.SuccessfulLoginFormState

@Composable
fun EnableBiometricLoginScreen(
    viewModel: LoginViewModel,
    onCancelClick: () -> Unit,
    onAuthorizeClick: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val loginFormState by viewModel.loginWithPasswordFormState.observeAsState()
    
    var usernameError: String? by remember { mutableStateOf(null) }
    var passwordError: String? by remember { mutableStateOf(null) }
    var isAuthorizeEnabled by remember { mutableStateOf(false) }
    
    // Update error states based on form state
    loginFormState?.let { formState ->
        when (formState) {
            is SuccessfulLoginFormState -> {
                isAuthorizeEnabled = formState.isDataValid
                usernameError = null
                passwordError = null
            }
            is FailedLoginFormState -> {
                isAuthorizeEnabled = false
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
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = stringResource(R.string.enable_biometric_login),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Description
        Text(
            text = stringResource(R.string.desc_biometrics_authorization),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
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
                    if (isAuthorizeEnabled) {
                        onAuthorizeClick(username, password)
                    }
                }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
            
            Button(
                onClick = { onAuthorizeClick(username, password) },
                enabled = isAuthorizeEnabled
            ) {
                Text(stringResource(R.string.btn_authorize))
            }
        }
    }
}
