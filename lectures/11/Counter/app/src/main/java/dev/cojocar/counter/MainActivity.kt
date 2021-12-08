package dev.cojocar.counter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.cojocar.counter.ui.theme.CounterTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      CounterTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
          MyScreenContent()
        }
      }
    }
  }
}

@Composable
fun MyScreenContent(names: List<String> = listOf("Android", "there")) {
  val counterState = remember { mutableStateOf(0) }

  Column(modifier = Modifier.fillMaxHeight()) {
    for (name in names) {
      Greeting(name = name)
      Divider(color = Color.Blue)
    }
    Counter(
      count = counterState.value,
      updateCount = { newCount ->
        counterState.value = newCount
      }
    )
  }
}

@Composable
fun Greeting(name: String) {
  Text(text = "Hello $name!", modifier = Modifier.padding(24.dp))
}

@Composable
fun Counter(count: Int, updateCount: (Int) -> Unit) {
  Button(
    onClick = { updateCount(count + 1) },
    colors = ButtonDefaults.buttonColors(
      backgroundColor = if (count > 3) Color.Green else Color.White
    )
  ) {
    Text("I've been clicked $count times")
  }
}

@Preview("MyScreen preview")
@Composable
fun DefaultPreview() {
  MyScreenContent()
}