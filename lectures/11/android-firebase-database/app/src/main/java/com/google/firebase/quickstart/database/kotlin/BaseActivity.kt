package com.google.firebase.quickstart.database.kotlin

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

  val uid: String
    get() = FirebaseAuth.getInstance().currentUser!!.uid
}
