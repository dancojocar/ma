package com.google.firebase.quickstart.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.ui.theme.FirebaseAuthTheme
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class GoogleSignInActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = Firebase.auth

        setContent {
            FirebaseAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoogleSignInScreen(auth)
                }
            }
        }
    }
}

@Composable
fun GoogleSignInScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var user by remember { mutableStateOf(auth.currentUser) }
    var isLoading by remember { mutableStateOf(false) }

    // Prepare Credential Manager
    val credentialManager = remember { CredentialManager.create(context) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Google Sign-In",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (user != null) {
                Text(text = "Signed in as: ${user?.email}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "UID: ${user?.uid}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        user = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            } else {
                Text(text = "Sign in to acquire the Google Badge", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            signInWithGoogle(context, credentialManager, auth) { signedInUser ->
                                user = signedInUser
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoading) "Signing in..." else "Sign in with Google")
                }
            }
        }
    }
}

private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    auth: FirebaseAuth,
    onResult: (com.google.firebase.auth.FirebaseUser?) -> Unit
) {
    // 1. Generate Nonce
    val ranNonce: String = UUID.randomUUID().toString()
    val bytes: ByteArray = ranNonce.toByteArray()
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    val digest: ByteArray = md.digest(bytes)
    val hashedNonce: String = digest.fold("") { str, it -> str + "%02x".format(it) }

    // 2. Configure Google ID Option
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .setNonce(hashedNonce)
        .build()

    // 3. Create Request
    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        // 4. Get Credential
        val result = credentialManager.getCredential(request = request, context = context)
        val credential = result.credential

        // 5. Check if it's a Google ID Token
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            
            // 6. Auth with Firebase
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            auth.signInWithCredential(authCredential)
                .addOnSuccessListener { authResult ->
                    Log.d("GoogleSignIn", "Firebase sign in success")
                    onResult(authResult.user)
                }
                .addOnFailureListener { e ->
                    Log.w("GoogleSignIn", "Firebase sign in failed", e)
                    Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }
        } else {
            Log.e("GoogleSignIn", "Unexpected credential type")
            onResult(null)
        }
    } catch (e: GetCredentialException) {
        Log.e("GoogleSignIn", "GetCredential failed", e)
        onResult(null)
    }
}
