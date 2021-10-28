package ro.cojocar.dan.portfolio

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ro.cojocar.dan.portfolio.databinding.ActivityPortfolioDetailBinding

/**
 * An activity representing a single Portfolio detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [PortfolioListActivity].
 */
class PortfolioDetailActivity : AppCompatActivity() {
  private lateinit var binding: ActivityPortfolioDetailBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPortfolioDetailBinding.inflate(layoutInflater)
    val rootView = binding.root
    setContentView(rootView)
    setSupportActionBar(binding.detailToolbar)

    binding.fab.setOnClickListener { view ->
      Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    if (savedInstanceState == null) {
      val fragment = PortfolioDetailFragment().apply {
        arguments = Bundle().apply {
          putString(
            PortfolioDetailFragment.ARG_ITEM_NAME,
            intent.getStringExtra(PortfolioDetailFragment.ARG_ITEM_NAME)
          )
        }
      }

      supportFragmentManager.beginTransaction()
        .add(R.id.portfolio_detail_container, fragment)
        .commit()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem) =
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
}
