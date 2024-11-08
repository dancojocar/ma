package ro.cojocar.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import ro.cojocar.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Column(modifier = Modifier.padding(innerPadding)) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            CounterList(
              title = "SharedFlow",
              flow = viewModel.sharedFlow,
              modifier = Modifier.height(screenHeight / 2)
            )
            CounterList(
              title = "RegularFlow",
              flow = viewModel.regularFlow,
              modifier = Modifier.height(screenHeight / 2)
            )
          }
        }
      }
    }
    Intent(this, MyService::class.java).also {
      startService(it)
    }
  }
}

@Composable
fun CounterList(title: String, flow: Flow<Int>, modifier: Modifier = Modifier) {
  val listState = rememberLazyListState()
  val count by flow.collectAsState(initial = 0)

  val items = remember { mutableStateListOf<Int>() }
  if (count != 0 && (items.isEmpty() || items.last() != count)) {
    items.add(count)
  }

  LaunchedEffect(items.size) {
    if (items.isNotEmpty()) {
      listState.scrollToItem(items.lastIndex)
    }
  }

  LazyColumn(
    state = listState,
    modifier = modifier
  ) {
    items(items) { item ->
      Text(text = "Collected $item from $title")
    }
  }
}