package com.example.biometric.feature.login

import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.authenticateWithClass3Biometrics
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.biometric.R
import com.example.biometric.data.model.Password
import com.example.biometric.data.model.Username
import com.example.biometric.feature.destinations.FeedScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import kotlin.random.Random

@Destination(start = true)
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val screenState = viewModel.loginState.collectAsState()

    Box {
        val snackbarState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LoginContent(
            onSubmitClicked = viewModel::triggerLogin,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )

        when (val state = screenState.value) {
            is LoginState.Failure ->
                SideEffect {
                    state.errorMessage?.let { message ->
                        scope.launch { snackbarState.showSnackbar(message) }
                    }
                }
            LoginState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Center))
            }
            LoginState.Success -> LaunchedEffect(key1 = Unit) {
                navigator.popBackStack()
                navigator.navigate(FeedScreenDestination)
            }
            is LoginState.ShowBiometrics.Encryption ->
                AskToSaveBiometrics(
                    cryptoObject = state.cryptoObject,
                    onAuthSuccess = viewModel::triggerEncryption,
                    onAuthError = { message ->
                        scope.launch {
                            snackbarState.showSnackbar(message)
                        }
                    }
                )
            is LoginState.ShowBiometrics.Decryption ->
                BiometricPrompt(
                    cryptoObject = state.cryptoObject,
                    promptTitle = "Login with Biometrics",
                    promptCancel = "Cancel",
                    onAuthSuccess = viewModel::triggerDecryption,
                    onAuthError = { message -> scope.launch { snackbarState.showSnackbar(message) } }
                )
            LoginState.Idle -> {
                /* no-op */
            }
        }

        SnackbarHost(hostState = snackbarState, Modifier.align(BottomCenter))
    }

    RegisterResumeStateListener(viewModel::onResume)
}

@Composable
private fun RegisterResumeStateListener(onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun AskToSaveBiometrics(
    cryptoObject: BiometricPrompt.CryptoObject,
    onAuthSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    onAuthError: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    var showBiometricPrompt by remember { mutableStateOf(false) }

    if (showDialog) {
        CredentialsDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                showBiometricPrompt = true
            }
        )
    }

    if (showBiometricPrompt) {
        BiometricPrompt(
            cryptoObject = cryptoObject,
            promptTitle = stringResource(id = R.string.biometric_prompt_title),
            promptCancel = stringResource(id = R.string.biometric_prompt_cancel),
            onAuthSuccess = onAuthSuccess,
            onAuthError = onAuthError
        )
    }
}

@Composable
private fun BiometricPrompt(
    cryptoObject: BiometricPrompt.CryptoObject,
    promptTitle: String,
    promptCancel: String,
    onAuthSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    onAuthError: (String) -> Unit
) {
    val activity = LocalContext.current as FragmentActivity
    LaunchedEffect(key1 = Random.nextInt()) {
        activity
            .runCatching {
                authenticateWithClass3Biometrics(
                    cryptoObject,
                    title = promptTitle,
                    negativeButtonText = promptCancel
                )
            }
            .fold(
                onSuccess = { onAuthSuccess(it) },
                onFailure = {
                    onAuthError("Biometric Auth Failed: ${it.message}. Please use conventional login")
                }
            )
    }
}

@Composable
fun LoginContent(
    onSubmitClicked: (Username, Password) -> Unit,
    modifier: Modifier
) {
    val (username, setUsername) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }

    Surface {
        Column(modifier = modifier) {
            OutlinedTextField(
                value = username,
                onValueChange = setUsername,
                placeholder = { Text(text = "Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = setPassword,
                placeholder = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()

            )
            Spacer(modifier = Modifier.padding(12.dp))
            Button(
                onClick = { onSubmitClicked(Username(username), Password(password)) },
                modifier = Modifier.align(CenterHorizontally)
            ) {
                Text("Login")
            }
        }
    }
}

@Composable
fun CredentialsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.use_biometrics_dialog))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.use_biometrics_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.use_biometrics_no))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginContent(onSubmitClicked = { username, password -> }, modifier = Modifier.padding(8.dp))
}