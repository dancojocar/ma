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

package com.android.example.filelocker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.filelocker.FileEntity
import com.android.example.filelocker.ui.viewmodels.FileLockerViewModel
import com.android.example.filelocker.ui.viewmodels.FileLockerViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onNavigateToEdit: (String) -> Unit,
    viewModel: FileLockerViewModel = viewModel(
        factory = FileLockerViewModelFactory(LocalContext.current)
    )
) {
    val files by viewModel.files.observeAsState(emptyList())
    val hasPassword by viewModel.hasPassword.observeAsState(false)
    val snackbarMessage by viewModel.snackbarMessage.observeAsState()
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var pendingFileToOpen by remember { mutableStateOf<FileEntity?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Locker") },
                actions = {
                    IconButton(onClick = {
                        if (hasPassword) {
                            showResetPasswordDialog = true
                        } else {
                            showSetPasswordDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Lock, contentDescription = "Password Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit("") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(files) { file ->
                FileListItem(
                    file = file,
                    onClick = {
                        if (hasPassword) {
                            pendingFileToOpen = file
                            showPasswordDialog = true
                        } else {
                            onNavigateToEdit(file.title)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
    
    if (showPasswordDialog) {
        PasswordDialog(
            title = "Enter Password",
            onDismiss = {
                showPasswordDialog = false
                pendingFileToOpen = null
            },
            onConfirm = { password ->
                if (viewModel.verifyPassword(password)) {
                    pendingFileToOpen?.let { onNavigateToEdit(it.title) }
                    showPasswordDialog = false
                    pendingFileToOpen = null
                } else {
                    viewModel.showMessage("Incorrect password")
                }
            }
        )
    }
    
    if (showSetPasswordDialog) {
        SetPasswordDialog(
            onDismiss = { showSetPasswordDialog = false },
            onConfirm = { password ->
                viewModel.setPassword(null, password)
                showSetPasswordDialog = false
            }
        )
    }
    
    if (showResetPasswordDialog) {
        ResetPasswordDialog(
            onDismiss = { showResetPasswordDialog = false },
            onConfirm = { currentPassword, newPassword ->
                viewModel.setPassword(currentPassword, newPassword)
                showResetPasswordDialog = false
            }
        )
    }
}

@Composable
fun FileListItem(
    file: FileEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = file.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Just now",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PasswordDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Password") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ResetPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentPassword, newPassword) }) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
