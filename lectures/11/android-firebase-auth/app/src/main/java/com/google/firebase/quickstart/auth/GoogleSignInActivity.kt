package com.google.firebase.quickstart.auth

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.quickstart.auth.databinding.ActivityGoogleBinding

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
class GoogleSignInActivity : BaseActivity(), View.OnClickListener {
  private lateinit var binding: ActivityGoogleBinding

  private lateinit var auth: FirebaseAuth

  private lateinit var googleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityGoogleBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Button listeners
    binding.signInButton.setOnClickListener(this)
    binding.signOutButton.setOnClickListener(this)
    binding.disconnectButton.setOnClickListener(this)

    // Configure Google Sign In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build()

    googleSignInClient = GoogleSignIn.getClient(this, gso)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
  }

  public override fun onStart() {
    super.onStart()
    // Check if user is signed in (non-null) and update UI accordingly.
    val currentUser = auth.currentUser
    updateUI(currentUser)
  }

  @Deprecated("Deprecated in Java")
  public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      try {
        // Google Sign In was successful, authenticate with Firebase
        val account = task.getResult(ApiException::class.java)
        firebaseAuthWithGoogle(account!!)
      } catch (e: ApiException) {
        // Google Sign In failed, update UI appropriately
        logw("Google sign in failed", e)
        updateUI(null)
      }
    }
  }

  private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    logd("firebaseAuthWithGoogle:" + acct.id!!)
    showProgressDialog()

    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
    auth.signInWithCredential(credential)
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          // Sign in success, update UI with the signed-in user's information
          logd("signInWithCredential:success")
          val user = auth.currentUser
          updateUI(user)
        } else {
          // If sign in fails, display a message to the user.
          logw("signInWithCredential:failure", task.exception)
          Snackbar.make(binding.root, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
          updateUI(null)
        }

        hideProgressDialog()
      }
  }

  private fun signIn() {
    val signInIntent = googleSignInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
  }

  private fun signOut() {
    // Firebase sign out
    auth.signOut()

    // Google sign out
    googleSignInClient.signOut().addOnCompleteListener(this) {
      updateUI(null)
    }
  }

  private fun revokeAccess() {
    // Firebase sign out
    auth.signOut()

    // Google revoke access
    googleSignInClient.revokeAccess().addOnCompleteListener(this) {
      updateUI(null)
    }
  }

  private fun updateUI(user: FirebaseUser?) {
    hideProgressDialog()
    if (user != null) {
      binding.status.text = getString(R.string.google_status_fmt, user.email)
      binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)

      binding.signInButton.visibility = View.GONE
      binding.signOutAndDisconnect.visibility = View.VISIBLE
    } else {
      binding.status.setText(R.string.signed_out)
      binding.detail.text = null

      binding.signInButton.visibility = View.VISIBLE
      binding.signOutAndDisconnect.visibility = View.GONE
    }
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.signInButton -> signIn()
      R.id.signOutButton -> signOut()
      R.id.disconnectButton -> revokeAccess()
    }
  }

  companion object {
    private const val RC_SIGN_IN = 9001
  }
}
