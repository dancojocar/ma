package com.example.flexiblelayout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flexiblelayout.ui.theme.FlexibleLayoutTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      FlexibleLayoutTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colors.background
        ) {
          BoxWithConstraintsDemo()
        }
      }
    }
  }
}

@Composable
fun BoxWithConstraintsDemo() {
  Column {
    Column {
      MyBoxWithConstraintsDemo()
    }

    Text(
      "Here we set the size to 200.dp",
      modifier = Modifier.padding(top = 20.dp)
    )
    Column(modifier = Modifier.size(200.dp)) {
      MyBoxWithConstraintsDemo()
    }
  }
}

@Composable
private fun MyBoxWithConstraintsDemo() {
  BoxWithConstraints {
    val boxWithConstraintsScope = this
    //You can use this scope to get the
    // minWidth, maxWidth, minHeight, maxHeight in dp and constraints

    Column {
      if (boxWithConstraintsScope.maxHeight >= 400.dp) {
        Text(
          "This is only visible when the maxHeight is >= 400.dp",
          style = TextStyle(fontSize = 20.sp)
        )
      }
      Column {
        Text(
          "minHeight: ${boxWithConstraintsScope.minHeight}"
        )
        Text(
          "maxHeight: ${boxWithConstraintsScope.maxHeight}"
        )
        Text(
          "minWidth: ${boxWithConstraintsScope.minWidth}"
        )
        Text(
          "maxWidth: ${boxWithConstraintsScope.maxWidth}"
        )
      }
    }
  }
}
