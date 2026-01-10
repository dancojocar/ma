package com.google.firebase.quickstart.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.ui.theme.FirebaseAuthTheme

class CustomAuthActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val customToken = mutableStateOf<String?>(null)

    private val tokenReceiver = object : TokenBroadcastReceiver() {
        override fun onNewToken(token: String?) {
            customToken.value = token
        }
    }

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
                    CustomAuthScreen(auth, customToken.value)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(tokenReceiver, TokenBroadcastReceiver.filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(tokenReceiver)
    }
}

@Composable
fun CustomAuthScreen(auth: FirebaseAuth, customToken: String?) {
    var user by remember { mutableStateOf(auth.currentUser) }
    var message by remember { mutableStateOf("") }
    
    // Auth Listener
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

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
                text = "Custom Auth Demo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (user != null) {
                Text(text = "Signed in as: ${user?.uid}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        auth.signOut()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            } else {
                Text(text = "Waiting for custom token from background...", style = MaterialTheme.typography.bodyMedium)
                if (customToken != null) {
                     Text(text = "Token Received: ${customToken.take(10)}...", color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        customToken?.let { token ->
                            auth.signInWithCustomToken(token)
                                .addOnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        message = "Sign in failed: ${task.exception?.message}"
                                    }
                                }
                        }
                    },
                    enabled = customToken != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In with Custom Token")
                }
            }
            
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
