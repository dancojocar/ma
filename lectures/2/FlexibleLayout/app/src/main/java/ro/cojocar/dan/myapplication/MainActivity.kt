package ro.cojocar.dan.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            text.text = getString(R.string.fromText, editText.text)
            button.text = getString(R.string.defaultText)
        }
        logd("onCreate called")
    }
}
