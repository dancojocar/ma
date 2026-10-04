package com.unieats.app.ui.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.app.R
import kotlinx.coroutines.launch

@Composable
fun UnlockScreen(
    onUsePassword: () -> Unit,
    viewModel: UnlockViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current as FragmentActivity
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    // The prompt is tied to this composition: leaving the screen cancels it (invokeOnCancellation).
    val scope = rememberCoroutineScope()
    val promptTitle = stringResource(R.string.unlock_title)
    val promptNegative = stringResource(R.string.unlock_prompt_negative)
    val unlock: () -> Unit = {
        scope.launch {
            viewModel.unlock { cipher -> activity.authenticate(cipher, promptTitle, promptNegative) }
        }
    }
    var prompted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!prompted) {
            prompted = true
            unlock()
        }
    }
    UnlockContent(displayName = displayName, onUnlock = unlock, onUsePassword = onUsePassword)
}

@Composable
fun UnlockContent(displayName: String, onUnlock: () -> Unit, onUsePassword: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Icon(
                painterResource(R.drawable.ic_fingerprint),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text(stringResource(R.string.unlock_title), style = MaterialTheme.typography.headlineSmall)
            if (displayName.isNotEmpty()) {
                Text(
                    stringResource(R.string.unlock_signed_in_as, displayName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onUnlock, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.unlock_action)) }
            TextButton(onClick = onUsePassword) { Text(stringResource(R.string.unlock_use_password_instead)) }
        }
    }
}
