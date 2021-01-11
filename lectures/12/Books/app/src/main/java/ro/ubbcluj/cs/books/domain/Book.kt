package ro.ubbcluj.cs.books.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "books")
data class Book(@field:PrimaryKey(autoGenerate = true)
                var id: Int, var title: String?, var date: Date?)
