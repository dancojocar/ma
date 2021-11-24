package com.google.firebase.quickstart.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.quickstart.auth.databinding.ActivityCustomBinding

/**
 * Demonstrate Firebase Authentication using a custom minted token. For more information, see:
 * https://firebase.google.com/docs/auth/android/custom-auth
 */
class CustomAuthActivity : AppCompatActivity(), View.OnClickListener {
  private lateinit var binding: ActivityCustomBinding

  private lateinit var auth: FirebaseAuth

  private var customToken: String? = null
  private lateinit var tokenReceiver: TokenBroadcastReceiver

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityCustomBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Button click listeners
    binding.buttonSignIn.setOnClickListener(this)

    // Create token receiver (for demo purposes only)
    tokenReceiver = object : TokenBroadcastReceiver() {
      override fun onNewToken(token: String?) {
        logd("onNewToken:$token")
        setCustomToken(token.toString())
      }
    }

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
  }

  public override fun onStart() {
    super.onStart()
    // Check if user is signed in (non-null) and update UI accordingly.
    val currentUser = auth.currentUser
    updateUI(currentUser)
  }

  override fun onResume() {
    super.onResume()
    registerReceiver(tokenReceiver, TokenBroadcastReceiver.filter)
  }

  override fun onPause() {
    super.onPause()
    unregisterReceiver(tokenReceiver)
  }

  private fun startSignIn() {
    // Initiate sign in with custom token
    customToken?.let {
      auth.signInWithCustomToken(it)
          .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
              // Sign in success, update UI with the signed-in user's information
              logd("signInWithCustomToken:success")
              val user = auth.currentUser
              updateUI(user)
            } else {
              // If sign in fails, display a message to the user.
              logw("signInWithCustomToken:failure", task.exception)
              Toast.makeText(baseContext, "Authentication failed.",
                  Toast.LENGTH_SHORT).show()
              updateUI(null)
            }
          }
    }
  }

  private fun updateUI(user: FirebaseUser?) {
    if (user != null) {
      binding.textSignInStatus.text = "User ID: $user.uid"
    } else {
      binding.textSignInStatus.text = "Error: sign in failed"
    }
  }

  private fun setCustomToken(token: String) {
    customToken = token

    val status = "Token:$customToken"

    // Enable/disable sign-in button and show the token
    binding.buttonSignIn.isEnabled = true
    binding.textTokenStatus.text = status
  }

  override fun onClick(v: View) {
    val i = v.id
    if (i == R.id.buttonSignIn) {
      startSignIn()
    }
  }
}
