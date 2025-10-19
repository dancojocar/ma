package ro.cojocar.sqldelight

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import androidx.compose.material.icons.filled.Refresh

@OptIn(ExperimentalMaterial3Api::class)
class ItemListActivity : AppCompatActivity() {

    private lateinit var queries: PlayerQueries
    private val playersFlow = MutableStateFlow<List<ChessPlayer>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queries = setupDatabase(applicationContext)
        playersFlow.value = queries.selectAll().executeAsList()

        setContent {
            val players by playersFlow.collectAsState()
            val context = LocalContext.current

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Chess Players") },
                            actions = {
                                IconButton(onClick = { reinsertData() }) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Re-insert Data")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { context.startActivity(Intent(context, AddEditPlayerActivity::class.java)) }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Player")
                        }
                    }
                ) { paddingValues ->
                    PlayerList(
                        players = players,
                        modifier = Modifier.padding(paddingValues),
                        onEdit = {
                            val intent = Intent(context, AddEditPlayerActivity::class.java).apply {
                                putExtra(AddEditPlayerActivity.ARG_ITEM_ID, it.player_number)
                            }
                            context.startActivity(intent)
                        },
                        onDelete = { player ->
                            queries.deletePlayer(player.player_number)
                            playersFlow.value = queries.selectAll().executeAsList()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        playersFlow.value = queries.selectAll().executeAsList()
    }

    private fun reinsertData() {
        queries.deleteAll()
        queries.insertPlayer(15, "Mikhail Tal", "You must take your opponent into a deep dark forest where 2+2=5, and the path leading out is only wide enough for one!")
        queries.insertPlayer(16, "Garry Kasparov", "I used to attack because it was the only thing I knew. Now I attack because I know it works best.")
        queries.insertPlayer(17, "Emanuel Lasker", "When you see a good move, look for a better one.")
        queries.insertPlayer(18, "H. G. Wells", "There is no remorse like the remorse of chess.")
        queries.insertPlayer(19, "Garry Kasparov", "Chess is life in miniature. Chess is a struggle, chess battles.")
        queries.insertPlayer(20, "Bill Hartston", "Chess doesn’t drive people mad, it keeps mad people sane.")
        queries.insertPlayer(21, "José Raúl Capablanca", "You may learn much more from a game you lose than from a game you win.")
        queries.insertPlayer(22, "Mikhail Chigorin", "Even a poor plan is better than no plan at all.")
        playersFlow.value = queries.selectAll().executeAsList()
    }
}

@Composable
fun PlayerList(
    players: List<ChessPlayer>,
    modifier: Modifier = Modifier,
    onEdit: (ChessPlayer) -> Unit,
    onDelete: (ChessPlayer) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(players) {
            player ->
            PlayerRow(player = player, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
fun PlayerRow(player: ChessPlayer, onEdit: (ChessPlayer) -> Unit, onDelete: (ChessPlayer) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(player) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = player.full_name, style = MaterialTheme.typography.headlineSmall)
            Text(text = player.quotes, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = { onDelete(player) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Player")
        }
    }
}