package com.example.dan.realm

import android.app.Application

import io.realm.Realm
import io.realm.RealmConfiguration

class MyApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    // Initialize Realm. Should only be done once
    // when the application starts.
    Realm.init(this)
  }
}
