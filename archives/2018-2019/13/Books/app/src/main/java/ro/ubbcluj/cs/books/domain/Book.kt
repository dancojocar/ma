package ro.ubbcluj.cs.books.domain

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.Date

@Entity(tableName = "books")
data class Book(@field:PrimaryKey(autoGenerate = true)
           var id: Int, var title: String?, var date: Date?)
