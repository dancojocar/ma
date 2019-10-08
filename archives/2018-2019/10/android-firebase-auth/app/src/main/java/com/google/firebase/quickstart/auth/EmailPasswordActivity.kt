package com.google.firebase.quickstart.auth

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_emailpassword.*

class EmailPasswordActivity : BaseActivity(), View.OnClickListener {

  private lateinit var auth: FirebaseAuth

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_emailpassword)

    // Buttons
    emailSignInButton.setOnClickListener(this)
    emailCreateAccountButton.setOnClickListener(this)
    signOutButton.setOnClickListener(this)
    verifyEmailButton.setOnClickListener(this)

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
            Toast.makeText(baseContext, "Authentication failed.",
                Toast.LENGTH_SHORT).show()
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
            Toast.makeText(baseContext, "Authentication failed.",
                Toast.LENGTH_SHORT).show()
            updateUI(null)
          }

          if (!task.isSuccessful) {
            status.setText(R.string.auth_failed)
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
    verifyEmailButton.isEnabled = false

    // Send verification email
    val user = auth.currentUser
    user?.sendEmailVerification()
        ?.addOnCompleteListener(this) { task ->
          // Re-enable button
          verifyEmailButton.isEnabled = true

          if (task.isSuccessful) {
            Toast.makeText(baseContext,
                "Verification email sent to ${user.email} ",
                Toast.LENGTH_SHORT).show()
          } else {
            loge("sendEmailVerification", task.exception)
            Toast.makeText(baseContext,
                "Failed to send verification email.",
                Toast.LENGTH_SHORT).show()
          }
        }
  }

  private fun validateForm(): Boolean {
    var valid = true

    val email = fieldEmail.text.toString()
    if (TextUtils.isEmpty(email)) {
      fieldEmail.error = "Required."
      valid = false
    } else {
      fieldEmail.error = null
    }

    val password = fieldPassword.text.toString()
    if (TextUtils.isEmpty(password)) {
      fieldPassword.error = "Required."
      valid = false
    } else {
      fieldPassword.error = null
    }

    return valid
  }

  private fun updateUI(user: FirebaseUser?) {
    hideProgressDialog()
    if (user != null) {
      status.text = getString(R.string.emailpassword_status_fmt,
          user.email, user.isEmailVerified)
      detail.text = getString(R.string.firebase_status_fmt, user.uid)

      emailPasswordButtons.visibility = View.GONE
      emailPasswordFields.visibility = View.GONE
      signedInButtons.visibility = View.VISIBLE

      verifyEmailButton.isEnabled = !user.isEmailVerified
    } else {
      status.setText(R.string.signed_out)
      detail.text = null

      emailPasswordButtons.visibility = View.VISIBLE
      emailPasswordFields.visibility = View.VISIBLE
      signedInButtons.visibility = View.GONE
    }
  }

  override fun onClick(v: View) {
    val i = v.id
    when (i) {
      R.id.emailCreateAccountButton -> createAccount(fieldEmail.text.toString(), fieldPassword.text.toString())
      R.id.emailSignInButton -> signIn(fieldEmail.text.toString(), fieldPassword.text.toString())
      R.id.signOutButton -> signOut()
      R.id.verifyEmailButton -> sendEmailVerification()
    }
  }
}
