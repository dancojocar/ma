package ro.cojocar.objectbox

import android.app.Application
import ro.cojocar.objectbox.store.ObjectBoxStore

class MyApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    ObjectBoxStore.init(this)
  }
}