package com.example.counter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Model
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.ExpandedHeight
import androidx.ui.layout.Spacing
import androidx.ui.material.Button
import androidx.ui.material.ContainedButtonStyle
import androidx.ui.material.Divider
import androidx.ui.material.MaterialTheme
import androidx.ui.material.surface.Surface
import androidx.ui.tooling.preview.Preview


@Model
class CounterState(var count: Int = 0)

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyApp {
        MyScreenContent()
      }
    }
  }
}

@Composable
fun MyApp(children: @Composable() () -> Unit) {
  MaterialTheme {
    Surface(color = Color.LightGray) {
      children()
    }
  }
}

@Composable
fun MyScreenContent(
    names: List<String> = listOf("Android", "Compose"),
    counterState: CounterState = CounterState()
) {
  Column(modifier = ExpandedHeight) {
    Column(modifier = Flexible(1f)) {
      for (name in names) {
        Greeting(name = name)
        Divider(color = Color.Green)
      }
    }
    Counter(counterState)
  }
}

@Composable
fun Greeting(name: String) {
  Text(
      text = "Hello $name!",
      modifier = Spacing(24.dp)
  )
}

@Composable
fun Counter(state: CounterState) {
  Button(
      text = "I've been clicked ${state.count} times",
      onClick = {
        state.count++
      },
      style = ContainedButtonStyle(color = if (state.count > 5) Color.Green else Color.White)
  )
}

@Preview("MyScreen preview")
@Composable
fun DefaultPreview() {
  MyApp {
    MyScreenContent()
  }
}