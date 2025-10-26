package ro.cojocar.dan.coroutinedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.cojocar.dan.coroutinedemo.ui.theme.CoroutineDemoTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      CoroutineDemoTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainScreen(viewModel)
        }
      }
    }
  }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
  val scrollState = rememberScrollState()
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .windowInsetsPadding(WindowInsets.systemBars),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val radioOptions = listOf(100, 500, 1000, 5000, 10000, 20000)
    val (selectedOption, onOptionSelected) = remember { mutableIntStateOf(radioOptions[0]) }

    Column {
      Text(text = "Workers: ${viewModel.workers.intValue}")
      radioOptions.forEach { workers ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .selectable(
              selected = (workers == selectedOption),
              onClick = {
                onOptionSelected(workers)
                viewModel.onWorkersChange(workers)
              }
            )
            .padding(horizontal = 16.dp)
        ) {
          RadioButton(
            selected = (workers == selectedOption),
            onClick = {
              onOptionSelected(workers)
              viewModel.onWorkersChange(workers)
            }
          )
          Text(
            text = workers.toString(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
      Button(onClick = { viewModel.onCoroutinesClick() }) {
        Text(text = "Coroutines")
      }
      Button(onClick = { viewModel.onThreadsClick() }) {
        Text(text = "Threads")
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    if (viewModel.progressVisible.value) {
      CircularProgressIndicator()
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = viewModel.resultText.value,
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  CoroutineDemoTheme {
    MainScreen(MainViewModel())
  }
}