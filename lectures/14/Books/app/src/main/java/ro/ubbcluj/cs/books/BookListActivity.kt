package ro.ubbcluj.cs.books

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ro.ubbcluj.cs.books.adapter.MyAdapter
import ro.ubbcluj.cs.books.databinding.ActivityBookListBinding
import ro.ubbcluj.cs.books.domain.Book
import ro.ubbcluj.cs.books.model.MainModel
import ro.ubbcluj.cs.books.utils.logd


/**
 * An activity representing a list of Events. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [EventDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class BookListActivity : AppCompatActivity() {

  private var adapter: MyAdapter? = null

  private lateinit var manager: Manager
  private lateinit var model: MainModel
  private lateinit var binding: ActivityBookListBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityBookListBinding.inflate(layoutInflater)
    setContentView(binding.root)
    model = ViewModelProvider(this).get(MainModel::class.java)
    manager = Manager()

    setSupportActionBar(binding.toolbar)
    binding.toolbar.title = title

    binding.fab.setOnClickListener {
      val intent = Intent(application, NewBook::class.java)
      startActivityForResult(intent, 10000)
    }

    binding.refresh.setOnClickListener {
      model.fetchBooksFromNetwork(application as BookApp)
    }

    setupRecyclerView(binding.recyclerView)
    observeModel()
    loadBooks()
  }

  private fun observeModel() {
    model.loading.observe { displayLoading(it) }
    model.books.observe { displayBooks(it ?: emptyList()) }
    model.message.observe { showError(it) }
  }

  private fun displayBooks(books: List<Book>) {
    adapter?.setData(books)
  }

  private fun displayLoading(loading: Boolean?) {
    logd("displayLoading: $loading")
    binding.progress.visibility = if (loading!!) View.VISIBLE else View.GONE
  }

  private fun <T> LiveData<T>.observe(observe: (T?) -> Unit) =
      observe(this@BookListActivity, { observe(it) })

  private fun loadBooks() {
    model.fetchBooks(application as BookApp)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    logd("Back in main activity")
  }


  private fun showError(message: String?) {
    binding.progress.visibility = View.GONE
    var errorMessage = "Unknown error"
    if (message != null) {
      errorMessage = message
    }
    Snackbar.make(binding.recyclerView, errorMessage, Snackbar.LENGTH_INDEFINITE)
        .setAction("RETRY") { loadBooks() }.show()
  }

  private fun setupRecyclerView(recyclerView: RecyclerView) {
    adapter = MyAdapter()
    (application as BookApp).db.bookDao.books
        .observe(this, { books ->
          if (books != null) {
            adapter!!.setData(books)
          }
        })
    recyclerView.adapter = adapter
  }
}
