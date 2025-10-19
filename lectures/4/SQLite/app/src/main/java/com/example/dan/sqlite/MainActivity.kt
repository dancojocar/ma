package com.example.dan.sqlite

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.example.dan.sqlite.NoteActivity.Companion.NOTE_ACTIVITY_CONTENT
import com.example.dan.sqlite.NoteActivity.Companion.NOTE_ACTIVITY_ID
import com.example.dan.sqlite.NoteActivity.Companion.NOTE_ACTIVITY_TITLE

class MainActivity : AppCompatActivity() {
    private val dbManager = NoteDbManager(this)

    private val noteActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val id = data.getIntExtra(NOTE_ACTIVITY_ID, 0)
                val title = data.getStringExtra(NOTE_ACTIVITY_TITLE)!!
                val content = data.getStringExtra(NOTE_ACTIVITY_CONTENT)!!
                val note = Note(id, title, content)
                processResultFromNoteActivity(note)
            }
        }
    }

    private val listNotes = mutableStateListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadQueryAll()
        setContent {
            MainScreen(
                notes = listNotes,
                onAddClick = {
                    val intent = Intent(this, NoteActivity::class.java)
                    noteActivityLauncher.launch(intent)
                },
                onNoteClick = {
                    toast("Click on ${it.title}")
                },
                onEditClick = {
                    updateNote(it)
                },
                onDeleteClick = {
                    deleteNote(it)
                }
            )
        }
    }

    private fun processResultFromNoteActivity(note: Note) {
        val found = listNotes.find { it.id == note.id }
        if (found != null) {
            val index = listNotes.indexOf(found)
            listNotes[index] = note
        } else {
            listNotes.add(note)
        }
    }

    override fun onDestroy() {
        dbManager.close()
        super.onDestroy()
    }

    private fun loadQueryAll() {
        val cursor = dbManager.queryAll()
        listNotes.clear()
        if (cursor.moveToFirst()) {
            do {
                val idColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_ID)
                val titleColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TITLE)
                val contentColumnIndex = cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_CONTENT)
                val id = cursor.getInt(idColumnIndex)
                val title = cursor.getString(titleColumnIndex)
                val content = cursor.getString(contentColumnIndex)
                listNotes.add(Note(id, title, content))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun updateNote(note: Note) {
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra(NOTE_ACTIVITY_ID, note.id)
        intent.putExtra(NOTE_ACTIVITY_TITLE, note.title)
        intent.putExtra(NOTE_ACTIVITY_CONTENT, note.content)
        noteActivityLauncher.launch(intent)
    }

    private fun deleteNote(note: Note) {
        val selectionArgs = arrayOf(note.id.toString())
        dbManager.delete(
            "${NoteContract.NoteEntry.COLUMN_ID}=?",
            selectionArgs
        )
        listNotes.remove(note)
    }
}
