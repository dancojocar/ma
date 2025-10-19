package com.example.dan.sqlite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(notes: List<Note>, onAddClick: () -> Unit, onNoteClick: (Note) -> Unit, onEditClick: (Note) -> Unit, onDeleteClick: (Note) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notes") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note")
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(notes) {
                NoteItem(note = it, onClick = onNoteClick, onEditClick = onEditClick, onDeleteClick = onDeleteClick)
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onClick: (Note) -> Unit, onEditClick: (Note) -> Unit, onDeleteClick: (Note) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(note)
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(note) }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = note.title, color = Color.Black, fontSize = 18.sp)
            Text(text = note.content)
        }
        Row {
            IconButton(onClick = { onEditClick(note) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Note")
            }
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Note")
            }
        }
    }
}
