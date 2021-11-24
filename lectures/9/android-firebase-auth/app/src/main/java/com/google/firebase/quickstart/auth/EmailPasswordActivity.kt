package com.google.firebase.quickstart.auth

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.quickstart.auth.databinding.ActivityEmailpasswordBinding

class EmailPasswordActivity : BaseActivity(), View.OnClickListener {
  private lateinit var binding: ActivityEmailpasswordBinding

  private lateinit var auth: FirebaseAuth

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityEmailpasswordBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Buttons
    binding.emailSignInButton.setOnClickListener(this)
    binding.emailCreateAccountButton.setOnClickListener(this)
    binding.signOutButton.setOnClickListener(this)
    binding.verifyEmailButton.setOnClickListener(this)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
  }

  public override fun onStart() {
    super.onStart()
    // Check if user is signed in (non-null) and update UI accordingly.
    val currentUser = auth.currentUser
    updateUI(currentUser)
  }

  private fun createAccount(email: String, password: String) {
    logd("createAccount:$email")
    if (!validateForm()) {
      return
    }

    showProgressDialog()

    auth.createUserWithEmailAndPassword(email, password)
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          // Sign in success, update UI with the signed-in user's information
          logd("createUserWithEmail:success")
          val user = auth.currentUser
          updateUI(user)
        } else {
          // If sign in fails, display a message to the user.
          logw("createUserWithEmail:failure", task.exception)
          Toast.makeText(
            baseContext, "Authentication failed.",
            Toast.LENGTH_SHORT
          ).show()
          updateUI(null)
        }

        hideProgressDialog()
      }
  }

  private fun signIn(email: String, password: String) {
    logd("signIn:$email")
    if (!validateForm()) {
      return
    }

    showProgressDialog()

    auth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          // Sign in success, update UI with the signed-in user's information
          logd("signInWithEmail:success")
          val user = auth.currentUser
          updateUI(user)
        } else {
          // If sign in fails, display a message to the user.
          logw("signInWithEmail:failure", task.exception)
          Toast.makeText(
            baseContext, "Authentication failed.",
            Toast.LENGTH_SHORT
          ).show()
          updateUI(null)
        }

        if (!task.isSuccessful) {
          binding.status.setText(R.string.auth_failed)
        }
        hideProgressDialog()
      }
  }

  private fun signOut() {
    auth.signOut()
    updateUI(null)
  }

  private fun sendEmailVerification() {
    // Disable button
    binding.verifyEmailButton.isEnabled = false

    // Send verification email
    val user = auth.currentUser
    user?.sendEmailVerification()
      ?.addOnCompleteListener(this) { task ->
        // Re-enable button
        binding.verifyEmailButton.isEnabled = true

        if (task.isSuccessful) {
          Toast.makeText(
            baseContext,
            "Verification email sent to ${user.email} ",
            Toast.LENGTH_SHORT
          ).show()
        } else {
          loge("sendEmailVerification", task.exception)
          Toast.makeText(
            baseContext,
            "Failed to send verification email.",
            Toast.LENGTH_SHORT
          ).show()
        }
      }
  }

  private fun validateForm(): Boolean {
    var valid = true

    val email = binding.fieldEmail.text.toString()
    if (TextUtils.isEmpty(email)) {
      binding.fieldEmail.error = "Required."
      valid = false
    } else {
      binding.fieldEmail.error = null
    }

    val password = binding.fieldPassword.text.toString()
    if (TextUtils.isEmpty(password)) {
      binding.fieldPassword.error = "Required."
      valid = false
    } else {
      binding.fieldPassword.error = null
    }

    return valid
  }

  private fun updateUI(user: FirebaseUser?) {
    hideProgressDialog()
    if (user != null) {
      binding.status.text = getString(
        R.string.emailpassword_status_fmt,
        user.email, user.isEmailVerified
      )
      binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)

      binding.emailPasswordButtons.visibility = View.GONE
      binding.emailPasswordFields.visibility = View.GONE
      binding.signedInButtons.visibility = View.VISIBLE

      binding.verifyEmailButton.isEnabled = !user.isEmailVerified
    } else {
      binding.status.setText(R.string.signed_out)
      binding.detail.text = null

      binding.emailPasswordButtons.visibility = View.VISIBLE
      binding.emailPasswordFields.visibility = View.VISIBLE
      binding.signedInButtons.visibility = View.GONE
    }
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.emailCreateAccountButton -> createAccount(
        binding.fieldEmail.text.toString(),
        binding.fieldPassword.text.toString()
      )
      R.id.emailSignInButton -> signIn(
        binding.fieldEmail.text.toString(),
        binding.fieldPassword.text.toString()
      )
      R.id.signOutButton -> signOut()
      R.id.verifyEmailButton -> sendEmailVerification()
    }
  }
}
