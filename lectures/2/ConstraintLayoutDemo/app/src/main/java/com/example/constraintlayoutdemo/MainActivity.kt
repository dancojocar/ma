package com.example.constraintlayoutdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.constraintlayoutdemo.ui.theme.ConstraintLayoutDemoTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      ConstraintLayoutDemoTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          ConstraintLayoutDemo()
        }
      }
    }
  }
}

@Composable
fun ConstraintLayoutDemo() {
  ConstraintLayout(modifier = Modifier.size(200.dp)) {
    val (redBox, blueBox, yellowBox, text) = createRefs()

    Box(modifier = Modifier
      .size(50.dp)
      .background(Color.Blue)
      .constrainAs(blueBox) {})

    Box(modifier = Modifier
      .size(50.dp)
      .background(Color.Yellow)
      .constrainAs(yellowBox) {
        top.linkTo(blueBox.bottom)
        start.linkTo(blueBox.end)
      })

    Box(modifier = Modifier
      .size(50.dp)
      .background(Color.Red)
      .constrainAs(redBox) {
        bottom.linkTo(yellowBox.bottom, 20.dp)
        start.linkTo(yellowBox.end)
      })

    Text("Hello World", modifier = Modifier.constrainAs(text) {
      top.linkTo(parent.top)
      start.linkTo(redBox.start)
    })

  }
}
