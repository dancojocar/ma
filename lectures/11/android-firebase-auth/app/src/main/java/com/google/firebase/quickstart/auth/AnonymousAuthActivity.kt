package com.google.firebase.quickstart.auth

import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.ui.theme.FirebaseAuthTheme

class AnonymousAuthActivity : ComponentActivity() {

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
                    AnonymousAuthScreen(auth)
                }
            }
        }
    }
}

@Composable
fun AnonymousAuthScreen(auth: FirebaseAuth) {
    var user by remember { mutableStateOf(auth.currentUser) }
    var message by remember { mutableStateOf("") }
    
    // For linking
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLinking by remember { mutableStateOf(false) }

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
                text = "Anonymous Auth",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (user != null) {
                Text(text = "Signed in as: Anonymouse", style = MaterialTheme.typography.bodyLarge)
                Text(text = "UID: ${user?.uid}", style = MaterialTheme.typography.bodySmall)
                if (user?.isAnonymous == false) {
                     Text(text = "Account is linked! (Permanent)", color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        user = null
                        message = ""
                        isLinking = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (user?.isAnonymous == true) {
                    if (!isLinking) {
                        Button(
                            onClick = { isLinking = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Link to Email/Password")
                        }
                    } else {
                        Text("Link Account", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (email.isNotEmpty() && password.isNotEmpty()) {
                                        val credential = EmailAuthProvider.getCredential(email, password)
                                        user?.linkWithCredential(credential)
                                            ?.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    message = "Account linked successfully!"
                                                    user = auth.currentUser // Refresh state
                                                    isLinking = false
                                                } else {
                                                    message = "Link failed: ${task.exception?.message}"
                                                }
                                            }
                                    }
                                }
                            ) {
                                Text("Link")
                            }
                            Button(onClick = { isLinking = false }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            } else {
                Text(text = "Sign in anonymously to access content.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        auth.signInAnonymously()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    user = auth.currentUser
                                    message = "Signed in anonymously."
                                } else {
                                    message = "Sign in failed: ${task.exception?.message}"
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In Anonymously")
                }
            }
            
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
