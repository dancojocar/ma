package ro.cojocar.viewbindingdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ro.cojocar.viewbindingdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.mainTitle.text = getString(R.string.mainTitle)
    binding.subTitle.text = getString(R.string.subTitle)
  }
}
