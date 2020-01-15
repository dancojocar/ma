package ro.ubbcluj.cs.books

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_book_detail.*


/**
 * An activity representing a single Book detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [BookListActivity].
 */
class EventDetailActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_detail)
    val toolbar = findViewById<Toolbar>(R.id.detail_toolbar)
    setSupportActionBar(toolbar)

    fab.setOnClickListener { view ->
      Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show()
    }

    // Show the Up button in the action bar.
    val actionBar = supportActionBar
    actionBar?.setDisplayHomeAsUpEnabled(true)

    if (savedInstanceState == null) {
      // Create the detail fragment and add it to the activity
      // using a fragment transaction.
      val bundle = intent.extras
      val fragment = EventDetailFragment()
      fragment.arguments = bundle
      supportFragmentManager.beginTransaction()
          .add(R.id.event_detail_container, fragment)
          .commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId
    if (id == android.R.id.home) {
      NavUtils.navigateUpTo(this, Intent(this, BookListActivity::class.java))
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
