package ro.cojocar.sqldelight

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.squareup.sqldelight.android.AndroidSqliteDriver

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ItemListActivity].
 */
class ItemDetailActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_item_detail)
    setSupportActionBar(findViewById(R.id.detail_toolbar))
    val receivedPlayerNumber = intent.getLongExtra(ItemDetailFragment.ARG_ITEM_ID, 0)
    val queries = setupDatabase(applicationContext)

    findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
      if (receivedPlayerNumber == 0L) {
        Snackbar.make(view, "This is not a valid player number", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show()
      } else {
        queries.deletePlayer(receivedPlayerNumber)
        Snackbar.make(view, "Removed item with id: $receivedPlayerNumber", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show()
      }
    }

    // Show the Up button in the action bar.
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    if (savedInstanceState == null) {
      // Create the detail fragment and add it to the activity
      // using a fragment transaction.
      val fragment = ItemDetailFragment().apply {
        arguments = Bundle().apply {
          putLong(
            ItemDetailFragment.ARG_ITEM_ID, receivedPlayerNumber
          )
        }
      }

      supportFragmentManager.beginTransaction().add(R.id.item_detail_container, fragment).commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
    android.R.id.home -> {
      // This ID represents the Home or Up button. In the case of this
      // activity, the Up button is shown. For
      // more details, see the Navigation pattern on Android Design:
      //
      // http://developer.android.com/design/patterns/navigation.html#up-vs-back

      navigateUpTo(Intent(this, ItemListActivity::class.java))
      true
    }

    else -> super.onOptionsItemSelected(item)
  }
}