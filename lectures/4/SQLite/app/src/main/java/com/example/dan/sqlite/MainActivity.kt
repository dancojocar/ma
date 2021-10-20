package com.example.dan.sqlite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dan.sqlite.databinding.ActivityMainBinding
import com.example.dan.sqlite.databinding.NoteBinding
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.dan.sqlite.NoteActivity.Companion.NOTE_ACTIVITY_CONTENT
import com.example.dan.sqlite.NoteActivity.Companion.NOTE_ACTIVITY_ID
import com.example.dan.sqlite.NoteActivity.Companion.NOTE_ACTIVITY_TITLE


class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private var listNotes = ArrayList<Note>()
  private val notesAdapter = NotesAdapter(this, listNotes)
  private val dbManager = NoteDbManager(this)

  private val noteActivityLauncher = registerForActivityResult(StartActivityForResult()) { result ->
    logd("Note response: ${result.resultCode}")
    if (result.resultCode == Activity.RESULT_OK) {
      val data = result.data
      if (data != null) {
        val dataExtra = data.extras
        if (dataExtra != null) {
          val id = dataExtra.getInt(NOTE_ACTIVITY_ID)
          val title = dataExtra.getString(NOTE_ACTIVITY_TITLE)!!
          val content = dataExtra.getString(NOTE_ACTIVITY_CONTENT)!!
          val note = Note(id, title, content)
          processResultFromNoteActivity(note)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    binding.lvNotes.onItemClickListener =
      AdapterView.OnItemClickListener { _, _, position, _ ->
        toast("Click on ${listNotes[position].title}")
      }
    loadQueryAll()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.addNote -> {
        logd("add new Note")
        val intent = Intent(this, NoteActivity::class.java)
        noteActivityLauncher.launch(intent)
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun processResultFromNoteActivity(note: Note) {
    val found = listNotes.find { it.id == note.id }
    if (found != null) {
      found.title = note.title
      found.content = note.content
      logd("Update $found with $note")
    } else {
      listNotes.add(note)
      logd("Added note $note")
    }
    notesAdapter.notifyDataSetChanged()
  }

  override fun onDestroy() {
    dbManager.close()
    super.onDestroy()
  }

  fun loadQueryAll() {
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
    binding.lvNotes.adapter = notesAdapter
  }

  inner class NotesAdapter(context: Context, private var notesList: ArrayList<Note>) :
    BaseAdapter() {
    private var context: Context? = context
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
      val view: View?
      val vh: ViewHolder
      if (convertView == null) {
        view = layoutInflater.inflate(R.layout.note, parent, false)
        vh = ViewHolder(view)
        view.tag = vh
        logd("set Tag for ViewHolder, position: $position")
      } else {
        view = convertView
        vh = view.tag as ViewHolder
      }
      val mNote = notesList[position]
      vh.binding.tvTitle.text = mNote.title
      vh.binding.tvContent.text = mNote.content
      vh.binding.ivEdit.setOnClickListener {
        updateNote(mNote)
      }
      vh.binding.ivDelete.setOnClickListener {
        val dbManager = NoteDbManager(this.context!!)
        val selectionArgs = arrayOf(mNote.id.toString())
        dbManager.delete(
          "${NoteContract.NoteEntry.COLUMN_ID}=?",
          selectionArgs
        )
        listNotes.remove(mNote)
        notifyDataSetChanged()
      }
      return view
    }

    override fun getItem(position: Int): Any {
      return notesList[position]
    }

    override fun getItemId(position: Int): Long {
      return position.toLong()
    }

    override fun getCount(): Int {
      return notesList.size
    }
  }

  private fun updateNote(note: Note) {
    val intent = Intent(this, NoteActivity::class.java)
    logd("Update note: $note")
    intent.putExtra(NOTE_ACTIVITY_ID, note.id)
    intent.putExtra(NOTE_ACTIVITY_TITLE, note.title)
    intent.putExtra(NOTE_ACTIVITY_CONTENT, note.content)
    noteActivityLauncher.launch(intent)
  }

  private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = NoteBinding.bind(view)
  }
}