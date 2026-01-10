package com.google.firebase.quickstart.auth

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.ui.theme.FirebaseAuthTheme
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : ComponentActivity() {

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
                    PhoneAuthScreen(auth, this)
                }
            }
        }
    }
}

@Composable
fun PhoneAuthScreen(auth: FirebaseAuth, activity: ComponentActivity) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var storedVerificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    
    var isVerificationInProgress by remember { mutableStateOf(false) }
    var isCodeSent by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf(auth.currentUser) }
    var message by remember { mutableStateOf("") }

    val context = LocalContext.current

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant verification or auto-retrieval
                isVerificationInProgress = false
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user = auth.currentUser
                            message = "Instant verification success!"
                        } else {
                            message = "Sign in failed: ${task.exception?.message}"
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isVerificationInProgress = false
                message = "Verification failed: ${e.message}"
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token
                isCodeSent = true
                isVerificationInProgress = false
                message = "Code sent. Please check your SMS."
            }
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
                text = "Phone Authentication",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (user != null) {
                Text(text = "Signed in as: ${user?.phoneNumber}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "UID: ${user?.uid}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        user = null
                        isCodeSent = false
                        phoneNumber = ""
                        verificationCode = ""
                        message = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            } else {
                if (!isCodeSent) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (e.g. +1...)" ) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (phoneNumber.isNotEmpty()) {
                                isVerificationInProgress = true
                                message = "Sending code..."
                                val options = PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(activity)
                                    .setCallbacks(callbacks)
                                    .build()
                                PhoneAuthProvider.verifyPhoneNumber(options)
                            } else {
                                message = "Please enter phone number."
                            }
                        },
                        enabled = !isVerificationInProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isVerificationInProgress) "Sending..." else "Verify Phone Number")
                    }
                } else {
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = { Text("Verification Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (verificationCode.isNotEmpty() && storedVerificationId.isNotEmpty()) {
                                isVerificationInProgress = true
                                val credential = PhoneAuthProvider.getCredential(storedVerificationId, verificationCode)
                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener { task ->
                                        isVerificationInProgress = false
                                        if (task.isSuccessful) {
                                            user = auth.currentUser
                                            message = "Signed in successfully."
                                        } else {
                                            message = "Sign in failed: ${task.exception?.message}"
                                        }
                                    }
                            }
                        },
                        enabled = !isVerificationInProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign In")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            isCodeSent = false
                            verificationCode = ""
                            message = ""
                        },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors() // Text button style
                    ) {
                        Text("Change Number")
                    }
                }
            }
            
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
