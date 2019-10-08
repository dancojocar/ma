package com.example.dan.sqlite

import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_note.*
import org.jetbrains.anko.toast

class NoteActivity : AppCompatActivity() {
    var id = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            id = bundle.getInt("MainActId", 0)
            if (id != 0) {
                edtTitle.setText(bundle.getString("MainActTitle"))
                edtContent.setText(bundle.getString("MainActContent"))
            }
        }

        btAdd.setOnClickListener {
            val dbManager = NoteDbManager(this)
            val values = ContentValues()
            values.put("Title", edtTitle.text.toString())
            values.put("Content", edtContent.text.toString())
            if (id == 0) {
                val mID = dbManager.insert(values)
                if (mID > 0) {
                    toast("Add note successfully!")
                    finish()
                } else {
                    toast("Fail to add note!")
                }
            } else {
                val selectionArs = arrayOf(id.toString())
                val mID = dbManager.update(values, "Id=?", selectionArs)

                if (mID > 0) {
                    toast("Add note successfully!")
                    finish()
                } else {
                    toast("Fail to add note!")
                }
            }
        }
    }
}
