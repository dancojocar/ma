package ro.cojocar.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ro.cojocar.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FlowCollectorScreen(viewModel)
      }
    }
  }
}

@Composable
fun FlowCollectorScreen(viewModel: MainViewModel) {
  val state by viewModel.uiState.collectAsState()

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Column(Modifier.padding(innerPadding)) {

      // Controls
      Row(
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Button(onClick = viewModel::startCollecting, enabled = !state.isCollecting) {
          Text("Start Collecting")
        }

        if (state.isCollecting) {
          Spacer(Modifier.width(16.dp))
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(20.dp))
            Text(
              "${state.completed}/${viewModel.totalCollectors}",
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      // Display values
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        FlowColumn("Regular Flow", state.regular1, state.regular2)
        FlowColumn("Shared Flow", state.shared1, state.shared2)
      }

      Spacer(Modifier.height(16.dp))

      // Status log
      LazyColumn(Modifier.padding(horizontal = 16.dp)) {
        items(state.messages) { msg ->
          Card(
            Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Text(msg, Modifier.padding(8.dp))
          }
        }
      }
    }
  }
}

@Composable
fun FlowColumn(title: String, value1: Int, value2: Int) {
  val style = MaterialTheme.typography.titleLarge
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(title, style = style)
    Text("Collector 1: $value1", style = style)
    Text("Collector 2: $value2", style = style)
  }
}
