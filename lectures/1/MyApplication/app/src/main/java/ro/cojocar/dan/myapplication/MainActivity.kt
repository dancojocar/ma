package ro.cojocar.dan.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logd("onCreate called")
    }

    override fun onStart() {
        super.onStart()
        logd("onStart called")
    }

    override fun onResume() {
        super.onResume()
        logd("onResume called")
    }

    override fun onPause() {
        super.onPause()
        logd("onPause called")
    }

    override fun onStop() {
        super.onStop()
        logd("onStop called")
    }

    override fun onRestart() {
        super.onRestart()
        logd("onRestart called")
    }

    override fun onDestroy() {
        super.onDestroy()
        logd("onDestroy called")
    }
}
