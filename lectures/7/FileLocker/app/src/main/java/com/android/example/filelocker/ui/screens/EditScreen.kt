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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.filelocker.ui.viewmodels.EditViewModel
import com.android.example.filelocker.ui.viewmodels.EditViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    fileTitle: String,
    onNavigateBack: () -> Unit,
    viewModel: EditViewModel = viewModel(
        factory = EditViewModelFactory(LocalContext.current, fileTitle)
    )
) {
    val title by viewModel.title.collectAsState()
    val body by viewModel.body.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.observeAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }
    
    BackHandler {
        viewModel.saveFile()
        onNavigateBack()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (fileTitle.isBlank()) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveFile()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (fileTitle.isNotBlank()) {
                        IconButton(onClick = {
                            viewModel.deleteFile()
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.saveFile()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = body,
                onValueChange = viewModel::updateBody,
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 10
            )
        }
    }
}
