package com.example.dan.sqlite

import android.provider.BaseColumns

object NoteContract {
    const val DB_NAME = "NotesDB"
    const val DB_VERSION = 1

    // Table contents are grouped together in an anonymous object.
    object NoteEntry : BaseColumns {
        const val DB_TABLE = "Notes"
        const val COLUMN_ID = "Id"
        const val COLUMN_TITLE = "Title"
        const val COLUMN_CONTENT = "Content"
    }
}