package ro.ubbcluj.cs.books

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View

import ro.ubbcluj.cs.books.adapter.MyAdapter
import ro.ubbcluj.cs.books.books.R
import kotlinx.android.synthetic.main.activity_book_list.*
import ro.ubbcluj.cs.books.utils.logd


/**
 * An activity representing a list of Events. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [EventDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class BookListActivity : AppCompatActivity(), MyCallback {

  private var adapter: MyAdapter? = null

  private var recyclerView: View? = null
  private var manager: Manager? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_list)
    manager = Manager(application as BookApp)

    setSupportActionBar(toolbar)
    toolbar.title = title

    recyclerView = findViewById(R.id.event_list)
    assert(recyclerView != null)
    setupRecyclerView((recyclerView as RecyclerView?)!!)
    loadEvents()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    logd("Back in main activity")
  }

  private fun loadEvents(): Boolean {
    val connectivity = manager!!.networkConnectivity(applicationContext)
    if (connectivity) {
      fab.show()
    } else {
      fab.hide()
      showError("No internet connection!")
    }
    manager!!.loadEvents(progress, this)
    return connectivity
  }

  override fun showError(message: String) {
    progress.visibility = View.GONE
    Snackbar.make(recyclerView!!, message, Snackbar.LENGTH_INDEFINITE)
        .setAction("RETRY") { loadEvents() }.show()
  }

  override fun clear() {
    adapter!!.clear()
  }


  fun onAddClick(view: View) {
    val intent = Intent(application, NewBook::class.java)
    startActivityForResult(intent, 10000)
  }

  fun onRefreshClick(view: View) {
    manager!!.loadEvents(progress, this)
  }

  private fun setupRecyclerView(recyclerView: RecyclerView) {
    adapter = MyAdapter()
    (application as BookApp).db.bookDao.books
        .observe(this, Observer { books ->
          if (books != null) {
            adapter!!.setData(books)
          }
        })
    recyclerView.adapter = adapter
  }
}
