package ro.cojocar.dan.recyclerview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import ro.cojocar.dan.recyclerview.databinding.ActivityItemDetailBinding
import ro.cojocar.dan.recyclerview.dummy.DummyContent
//import kotlinx.android.synthetic.main.activity_item_detail.*

class ItemDetailActivity : AppCompatActivity() {
  private lateinit var binding: ActivityItemDetailBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityItemDetailBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    setSupportActionBar(binding.detailToolbar)

    binding.fab.setOnClickListener {
      val resultIntent = Intent()
      resultIntent.putExtra(
        "resultKey",
        "Something nice from: ${intent.getStringExtra(ItemDetailFragment.ARG_ITEM_ID)}"
      )
      setResult(Activity.RESULT_OK, resultIntent)
      finish()
    }

    // Show the Up button in the action bar.
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val itemId = intent.getStringExtra(ItemDetailFragment.ARG_ITEM_ID)
    if (itemId != null) {
      val item = DummyContent.ITEM_MAP[itemId]
      binding.toolbarLayout.title = item?.content
    }

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
