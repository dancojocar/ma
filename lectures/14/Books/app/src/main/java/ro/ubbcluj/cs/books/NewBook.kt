package ro.ubbcluj.cs.books

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_new_book.*
import ro.ubbcluj.cs.books.domain.Book
import ro.ubbcluj.cs.books.model.MainModel
import java.util.*

class NewBook : AppCompatActivity() {
  private lateinit var model: MainModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_book)
    model = ViewModelProviders.of(this).get(MainModel::class.java)
    save.setOnClickListener {
      val app: BookApp = application as BookApp
      model.addBook(app, Book(0, bookTitle.text.toString(), Date()))
      finish()
    }
  }
}
