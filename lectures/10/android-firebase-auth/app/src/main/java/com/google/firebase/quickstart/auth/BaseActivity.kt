package com.google.firebase.quickstart.auth

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

  @VisibleForTesting
  val progressDialog by lazy {
    ProgressBar(this)
  }

  fun showProgressDialog() {
    progressDialog.isIndeterminate = true
    progressDialog.visibility = View.VISIBLE
  }

  fun hideProgressDialog() {
    progressDialog.visibility = View.GONE
  }

  fun hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
  }

  public override fun onStop() {
    super.onStop()
    hideProgressDialog()
  }
}
