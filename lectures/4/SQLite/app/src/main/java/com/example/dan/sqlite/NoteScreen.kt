package com.example.dan.sqlite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NoteScreen(
    title: String,
    content: String,
    buttonText: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Note title") },
            modifier = Modifier.padding(top = 30.dp)
        )
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("Note content") },
            modifier = Modifier.padding(top = 20.dp)
        )
        Button(
            onClick = onButtonClick,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteScreenPreview() {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    NoteScreen(
        title = title,
        content = content,
        buttonText = "Add",
        onTitleChange = { title = it },
        onContentChange = { content = it },
        onButtonClick = {}
    )
}