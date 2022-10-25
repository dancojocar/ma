package com.example.lazycolumndemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.lazycolumndemo.ui.theme.LazyColumnDemoTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LazyColumnDemoTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colors.background
        ) {
          LazyColumnDemo()
        }
      }
    }
  }
}

@Composable
fun LazyColumnDemo() {
  val list = listOf(
    "A", "B", "C", "D"
  ) + ((0..50).map { it.toString() })
  LazyColumn(
    modifier = Modifier.fillMaxHeight()
  ) {
    items(list) { item ->
      ListItem(item)
    }
  }
}

@Composable
private fun ListItem(item: String) {
  logd("This get rendered $item")
  when (item) {
    "A" -> {
      Text(text = item, style = TextStyle(fontSize = 80.sp))
    }
    "B" -> {
      Button(onClick = {}) {
        Text(text = item, style = TextStyle(fontSize = 80.sp))
      }
    }
    "C" -> {
      //Do Nothing
    }
    "D" -> {
      Text(text = item, style = LocalTextStyle.current)
    }
    else -> {
      Text(text = item, style = TextStyle(fontSize = 80.sp))
    }
  }
}
