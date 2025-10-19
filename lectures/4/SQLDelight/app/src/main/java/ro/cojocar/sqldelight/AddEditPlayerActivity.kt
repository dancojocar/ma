package ro.cojocar.sqldelight

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.sqldelight.android.AndroidSqliteDriver

@OptIn(ExperimentalMaterial3Api::class)
class AddEditPlayerActivity : AppCompatActivity() {

    private lateinit var queries: PlayerQueries
    private var playerId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queries = setupDatabase(applicationContext)
        playerId = intent.getLongExtra(ARG_ITEM_ID, 0L)

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (playerId == 0L) "Add Player" else "Edit Player") }
                        )
                    }
                ) { paddingValues ->
                    val player = if (playerId == 0L) null else queries.selectById(playerId).executeAsOneOrNull()
                    var name by remember { mutableStateOf(player?.full_name ?: "") }
                    var quote by remember { mutableStateOf(player?.quotes ?: "") }

                    Column(modifier = Modifier.padding(paddingValues)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        OutlinedTextField(
                            value = quote,
                            onValueChange = { quote = it },
                            label = { Text("Quote") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        Button(
                            onClick = {
                                if (playerId == 0L) {
                                    queries.insertPlayer(queries.countAll().executeAsOne() + 1, name, quote)
                                } else {
                                    queries.updatePlayer(name, quote, playerId)
                                }
                                finish()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}