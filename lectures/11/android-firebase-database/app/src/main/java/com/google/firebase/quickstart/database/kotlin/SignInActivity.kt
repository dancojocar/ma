package com.google.firebase.quickstart.database.kotlin

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.databinding.ActivitySignInBinding
import com.google.firebase.quickstart.database.kotlin.models.User

class SignInActivity : BaseActivity(), View.OnClickListener {

  private lateinit var database: DatabaseReference
  private lateinit var auth: FirebaseAuth
  private lateinit var binding: ActivitySignInBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySignInBinding.inflate(layoutInflater)
    setContentView(binding.root)

    database = FirebaseDatabase.getInstance().reference
    auth = FirebaseAuth.getInstance()

    // Click listeners
    binding.buttonSignIn.setOnClickListener(this)
    binding.buttonSignUp.setOnClickListener(this)
  }

  public override fun onStart() {
    super.onStart()

    // Check auth on Activity start
    auth.currentUser?.let {
      onAuthSuccess(it)
    }
  }

  private fun signIn() {
    logd("signIn")
    if (!validateForm()) {
      return
    }

    showProgressDialog()
    val email = binding.fieldEmail.text.toString()
    val password = binding.fieldPassword.text.toString()

    auth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener(this) { task ->
        logd("signIn:onComplete:" + task.isSuccessful)
        hideProgressDialog()

        if (task.isSuccessful) {
          onAuthSuccess(task.result?.user!!)
        } else {
          loge("signIn failed: ", task.exception)
          Toast.makeText(
            baseContext, "Sign In Failed",
            Toast.LENGTH_SHORT
          ).show()
        }
      }
  }

  private fun signUp() {
    logd("signUp")
    if (!validateForm()) {
      return
    }

    showProgressDialog()
    val email = binding.fieldEmail.text.toString()
    val password = binding.fieldPassword.text.toString()

    auth.createUserWithEmailAndPassword(email, password)
      .addOnCompleteListener(this) { task ->
        logd("createUser:onComplete:" + task.isSuccessful)
        hideProgressDialog()

        if (task.isSuccessful) {
          onAuthSuccess(task.result?.user!!)
        } else {
          loge("createUser failed: ", task.exception)
          Toast.makeText(
            baseContext, "Sign Up Failed",
            Toast.LENGTH_SHORT
          ).show()
        }
      }
  }

  private fun onAuthSuccess(user: FirebaseUser) {
    val username = usernameFromEmail(user.email!!)

    // Write new user
    writeNewUser(user.uid, username, user.email)

    // Go to MainActivity
    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
    finish()
  }

  private fun usernameFromEmail(email: String): String {
    return if (email.contains("@")) {
      email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    } else {
      email
    }
  }

  private fun validateForm(): Boolean {
    var result = true
    if (TextUtils.isEmpty(binding.fieldEmail.text.toString())) {
      binding.fieldEmail.error = "Required"
      result = false
    } else {
      binding.fieldEmail.error = null
    }

    if (TextUtils.isEmpty(binding.fieldPassword.text.toString())) {
      binding.fieldPassword.error = "Required"
      result = false
    } else {
      binding.fieldPassword.error = null
    }

    return result
  }

  private fun writeNewUser(userId: String, name: String, email: String?) {
    val user = User(name, email)
    database.child("users").child(userId).setValue(user)
  }

  override fun onClick(v: View) {
    val i = v.id
    if (i == R.id.buttonSignIn) {
      signIn()
    } else if (i == R.id.buttonSignUp) {
      signUp()
    }
  }

  private fun showProgressDialog() {
    binding.progressBar.visibility = View.VISIBLE
  }

  private fun hideProgressDialog() {
    binding.progressBar.visibility = View.GONE
  }
}
