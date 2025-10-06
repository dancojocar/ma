package ro.cojocar.dan.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ro.cojocar.dan.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    binding.button.setOnClickListener {
      val message = getString(R.string.fromText, binding.editText.text)
      //initial api
      binding.text.text = message
      //using synthetic import
      binding.button.text = getString(R.string.defaultText)
      //using new recommmended view binding way
      binding.text2.text = message
    }
    logd("onCreate called")
  }
}
