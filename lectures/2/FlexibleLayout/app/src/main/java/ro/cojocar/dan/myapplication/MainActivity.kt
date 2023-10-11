package ro.cojocar.dan.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ro.cojocar.dan.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    button.setOnClickListener {
      val message = getString(R.string.fromText, editText.text)
      //initial api
      findViewById<TextView>(R.id.text).text = message
      //using synthetic import
      button.text = getString(R.string.defaultText)
      //using new recommmended view binding way
      binding.text2.text = message
    }
    logd("onCreate called")
  }
}
