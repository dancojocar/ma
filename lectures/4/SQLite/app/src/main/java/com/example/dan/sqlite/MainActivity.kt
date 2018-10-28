package com.example.dan.sqlite

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {
    private var listNotes = ArrayList<Note>()
    private val dbManager = NoteDbManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lvNotes.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    toast("Click on ${listNotes[position].title}")
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.addNote -> {
                    val intent = Intent(this, NoteActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loadQueryAll()
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
                val id = cursor.getInt(cursor.getColumnIndex("Id"))
                val title = cursor.getString(cursor.getColumnIndex("Title"))
                val content = cursor.getString(cursor.getColumnIndex("Content"))
                listNotes.add(Note(id, title, content))
            } while (cursor.moveToNext())
        }
        val notesAdapter = NotesAdapter(this, listNotes)
        lvNotes.adapter = notesAdapter
    }

    inner class NotesAdapter(context: Context, private var notesList: ArrayList<Note>) : BaseAdapter() {
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
            vh.tvTitle.text = mNote.title
            vh.tvContent.text = mNote.content
            vh.ivEdit.setOnClickListener {
                updateNote(mNote)
            }
            vh.ivDelete.setOnClickListener {
                val dbManager = NoteDbManager(this.context!!)
                val selectionArgs = arrayOf(mNote.id.toString())
                dbManager.delete("Id=?", selectionArgs)
                loadQueryAll()
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
        intent.putExtra("MainActId", note.id)
        intent.putExtra("MainActTitle", note.title)
        intent.putExtra("MainActContent", note.content)
        startActivity(intent)
    }

    private class ViewHolder(view: View?) {
        val tvTitle: TextView = view?.findViewById(R.id.tvTitle) as TextView
        val tvContent: TextView = view?.findViewById(R.id.tvContent) as TextView
        val ivEdit: ImageView = view?.findViewById(R.id.ivEdit) as ImageView
        val ivDelete: ImageView = view?.findViewById(R.id.ivDelete) as ImageView
    }
}