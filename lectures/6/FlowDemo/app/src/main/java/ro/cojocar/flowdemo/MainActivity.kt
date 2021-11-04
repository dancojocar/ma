package ro.cojocar.flowdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ro.cojocar.flowdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),
  CoroutineScope by CoroutineScope(Dispatchers.Main) {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    launch {
      ticTac().collect { binding.textHolder.text = it }
    }
  }
}

private fun ticTac() = flow {
  var isTic = false
  while (true) {
    emit(if (isTic) "Tic" else "Tac")
    delay(1000)
    isTic = !isTic
  }
}
