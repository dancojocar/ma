package com.google.firebase.quickstart.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.quickstart.auth.databinding.ActivityFacebookBinding

/**
 * Demonstrate Firebase Authentication using a Facebook access token.
 */
class FacebookLoginActivity : BaseActivity(), View.OnClickListener {
  private lateinit var binding: ActivityFacebookBinding

  private lateinit var auth: FirebaseAuth

  private lateinit var callbackManager: CallbackManager

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFacebookBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.buttonFacebookSignout.setOnClickListener(this)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()

    // Initialize Facebook Login button
    callbackManager = CallbackManager.Factory.create()

    binding.buttonFacebookLogin.setReadPermissions("email", "public_profile")
    binding.buttonFacebookLogin.registerCallback(
      callbackManager,
      object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
          logd("facebook:onSuccess:$loginResult")
          handleFacebookAccessToken(loginResult.accessToken)
        }

        override fun onCancel() {
          logd("facebook:onCancel")
          updateUI(null)
        }

        override fun onError(error: FacebookException) {
          logd("facebook:onError", error)
          updateUI(null)
        }
      })
  }

  public override fun onStart() {
    super.onStart()
    // Check if user is signed in (non-null) and update UI accordingly.
    val currentUser = auth.currentUser
    updateUI(currentUser)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    // Pass the activity result back to the Facebook SDK
    callbackManager.onActivityResult(requestCode, resultCode, data)
  }

  private fun handleFacebookAccessToken(token: AccessToken) {
    logd("handleFacebookAccessToken:$token")
    showProgressDialog()

    val credential = FacebookAuthProvider.getCredential(token.token)
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
          Toast.makeText(
            baseContext, "Authentication failed.",
            Toast.LENGTH_SHORT
          ).show()
          updateUI(null)
        }

        hideProgressDialog()
      }
  }

  fun signOut() {
    auth.signOut()
    LoginManager.getInstance().logOut()

    updateUI(null)
  }

  private fun updateUI(user: FirebaseUser?) {
    hideProgressDialog()
    if (user != null) {
      binding.status.text = getString(R.string.facebook_status_fmt, user.displayName)
      binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)

      binding.buttonFacebookLogin.visibility = View.GONE
      binding.buttonFacebookSignout.visibility = View.VISIBLE
    } else {
      binding.status.setText(R.string.signed_out)
      binding.detail.text = null

      binding.buttonFacebookLogin.visibility = View.VISIBLE
      binding.buttonFacebookSignout.visibility = View.GONE
    }
  }

  override fun onClick(v: View) {
    val i = v.id
    if (i == R.id.buttonFacebookSignout) {
      signOut()
    }
  }
}
