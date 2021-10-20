package com.example.dan.sqlite

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dan.sqlite.databinding.ActivityNoteBinding

class NoteActivity : AppCompatActivity() {

  companion object {
    const val NOTE_ACTIVITY_ID = "MainActId"
    const val NOTE_ACTIVITY_TITLE = "MainActTitle"
    const val NOTE_ACTIVITY_CONTENT = "MainActContent"
  }

  private lateinit var binding: ActivityNoteBinding
  private var id = 0
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityNoteBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    val bundle: Bundle? = intent.extras
    if (bundle != null) {
      id = bundle.getInt(NOTE_ACTIVITY_ID, 0)
      if (id != 0) {
        binding.edtTitle.setText(bundle.getString(NOTE_ACTIVITY_TITLE))
        binding.edtContent.setText(bundle.getString(NOTE_ACTIVITY_CONTENT))
        binding.btAdd.text = getString(R.string.noteUpdate)
      }
    }

    binding.btAdd.setOnClickListener {
      val dbManager = NoteDbManager(this)
      val values = ContentValues()
      val title = binding.edtTitle.text.toString()
      values.put(NoteContract.NoteEntry.COLUMN_TITLE, title)
      val content = binding.edtContent.text.toString()
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
  }
}
