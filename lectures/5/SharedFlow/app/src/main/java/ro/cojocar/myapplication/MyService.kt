package ro.cojocar.myapplication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class MyService : Service() {
  override fun onBind(p0: Intent?): IBinder? {
    return null;
  }

  override fun onCreate() {
    super.onCreate()
    thread {
      while (true) {
        println("Running")
        sleep(1000)
      }
    }
  }
}