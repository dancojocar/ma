package ro.cojocar.dan.recyclerview

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_item_detail.*

class ItemDetailActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_item_detail)
    setSupportActionBar(detail_toolbar)

    fab.setOnClickListener { view ->
      Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show()
    }

    // Show the Up button in the action bar.
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    if (savedInstanceState == null) {
      // Create the detail fragment and add it to the activity
      // using a fragment transaction.
      val fragment = ItemDetailFragment().apply {
        arguments = Bundle().apply {
          putString(
              ItemDetailFragment.ARG_ITEM_ID,
              intent.getStringExtra(ItemDetailFragment.ARG_ITEM_ID)
          )
        }
      }

      supportFragmentManager.beginTransaction()
          .add(R.id.item_detail_container, fragment)
          .commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem) =
      when (item.itemId) {
        android.R.id.home -> {
          // This ID represents the Home or Up button. In the case of this
          // activity, the Up button is shown. Use NavUtils to allow users
          // to navigate up one level in the application structure. For
          // more details, see the Navigation pattern on Android Design:
          //
          // http://developer.android.com/design/patterns/navigation.html#up-vs-back

          NavUtils.navigateUpTo(this, Intent(this, ItemListActivity::class.java))
          true
        }
        else -> super.onOptionsItemSelected(item)
      }
}
