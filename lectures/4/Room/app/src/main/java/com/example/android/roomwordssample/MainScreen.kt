package com.example.android.roomwordssample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(wordViewModel: WordViewModel, onAddWord: () -> Unit, onEditWord: (String) -> Unit) {
    val words = wordViewModel.allWords.observeAsState(listOf()).value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RoomWordSample") },
                actions = {
                    IconButton(onClick = { wordViewModel.deleteAll() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset DB")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddWord) {
                Icon(Icons.Filled.Add, contentDescription = "Add Word")
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(words) {
                word -> WordListItem(word.word, onDelete = {
                wordViewModel.delete(word.word)
            }, onEdit = {
                onEditWord(word.word)
            })
            }
        }
    }
}

@Composable
fun WordListItem(word: String, onDelete: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = word, modifier = Modifier.weight(1f))
        Button(onClick = onEdit) {
            Text(text = "Edit")
        }
        Button(onClick = onDelete) {
            Text(text = "Delete")
        }
    }
}
