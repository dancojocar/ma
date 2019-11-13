package ro.cojocar.flowdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    launch {
      ticTac().collect { textHolder.text = it }
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
