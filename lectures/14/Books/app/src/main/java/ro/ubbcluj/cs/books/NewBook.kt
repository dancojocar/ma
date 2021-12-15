package ro.ubbcluj.cs.books

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ro.ubbcluj.cs.books.databinding.ActivityNewBookBinding
import ro.ubbcluj.cs.books.domain.Book
import ro.ubbcluj.cs.books.model.MainModel
import java.util.*

class NewBook : AppCompatActivity() {
  private lateinit var model: MainModel
  private lateinit var binding: ActivityNewBookBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityNewBookBinding.inflate(layoutInflater)
    setContentView(binding.root)
    model = ViewModelProvider(this).get(MainModel::class.java)
    binding.save.setOnClickListener {
      val app: BookApp = application as BookApp
      model.addBook(app, Book(0,binding.bookTitle.text.toString(), Date()))
      finish()
    }
  }
}
