package com.example.dan.sqlite

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class NoteActivity : AppCompatActivity() {

  companion object {
    const val NOTE_ACTIVITY_ID = "MainActId"
    const val NOTE_ACTIVITY_TITLE = "MainActTitle"
    const val NOTE_ACTIVITY_CONTENT = "MainActContent"
  }

  private var id = 0
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val bundle: Bundle? = intent.extras
    val initialTitle = bundle?.getString(NOTE_ACTIVITY_TITLE) ?: ""
    val initialContent = bundle?.getString(NOTE_ACTIVITY_CONTENT) ?: ""
    id = bundle?.getInt(NOTE_ACTIVITY_ID, 0) ?: 0

    setContent {
      var title by remember { mutableStateOf(initialTitle) }
      var content by remember { mutableStateOf(initialContent) }
      val buttonText = if (id != 0) getString(R.string.noteUpdate) else "Add"

      NoteScreen(
        title = title,
        content = content,
        buttonText = buttonText,
        onTitleChange = { title = it },
        onContentChange = { content = it },
        onButtonClick = {
          val dbManager = NoteDbManager(this)
          val values = ContentValues()
          values.put(NoteContract.NoteEntry.COLUMN_TITLE, title)
          values.put(NoteContract.NoteEntry.COLUMN_CONTENT, content)
          val mId: Long
          if (id == 0) {
            id = dbManager.insert(values).toInt()
            mId = id.toLong()
          } else {
            val selectionArs = arrayOf(id.toString())
            mId = dbManager.update(
              values,
              "${NoteContract.NoteEntry.COLUMN_ID}=?", selectionArs
            ).toLong()
          }
          if (mId > 0) {
            toast("Add note successfully!")
            val response = Intent()
            val note = Note(id, title, content)
            response.putExtra(NOTE_ACTIVITY_ID, note.id)
            response.putExtra(NOTE_ACTIVITY_TITLE, note.title)
            response.putExtra(NOTE_ACTIVITY_CONTENT, note.content)
            setResult(Activity.RESULT_OK, response)
            finish()
          } else {
            toast("Fail to add/update the note!")
          }
        }
      )
    }
  }
}