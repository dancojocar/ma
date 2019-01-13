package ro.ubbcluj.cs.books

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View

import java.util.Date

import ro.ubbcluj.cs.books.books.R
import ro.ubbcluj.cs.books.domain.Book
import kotlinx.android.synthetic.main.activity_new_book.*

class NewBook : AppCompatActivity(), MyCallback {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_book)
  }

  fun save(view: View) {
    val manager = Manager(application as BookApp)
    manager.save(Book(0, bookTitle.text.toString(), Date()), this)
  }

  override fun showError(message: String) {
    Snackbar.make(bookTitle, message, Snackbar.LENGTH_INDEFINITE)
        .setAction("DISMISS") { finish() }.show()
  }

  override fun clear() {
    finish()
  }
}
