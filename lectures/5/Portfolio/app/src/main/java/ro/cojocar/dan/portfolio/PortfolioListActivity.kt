package ro.cojocar.dan.portfolio

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ro.cojocar.dan.portfolio.adapters.SimpleItemRecyclerViewAdapter
import ro.cojocar.dan.portfolio.databinding.ActivityPortfolioListBinding
import ro.cojocar.dan.portfolio.domain.Portfolio
import ro.cojocar.dan.portfolio.models.MainModel

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [PortfolioDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class PortfolioListActivity : AppCompatActivity() {

  private lateinit var binding: ActivityPortfolioListBinding
  private val model: MainModel by viewModels()
  private lateinit var adapter: SimpleItemRecyclerViewAdapter

  /**
   * Whether or not the activity is in two-pane mode, i.e. running on a tablet
   * device.
   */
  private var twoPane: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPortfolioListBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    setSupportActionBar(binding.toolbar)
    binding.toolbar.title = title

    binding.fab.setOnClickListener {
      GlobalScope.launch(Dispatchers.IO) {
        if (model.auth()) {
          model.fetchData()
          binding.fab.setImageResource(android.R.drawable.ic_input_get)
        }
      }
    }

    if (binding.portfolioListId.portfolioDetailContainer != null) {
      twoPane = true
    }

    setupRecyclerView(binding.portfolioListId.portfolioList)
    observeModel()
  }

  private fun displayLoading(loading: Boolean) {
    logd("displayLoading: $loading")
    binding.progress.visibility = if (loading) VISIBLE else GONE
  }

  private fun displayPortfolios(portfolios: List<Portfolio>) {
    adapter.portfolios = portfolios
  }

  private fun displayMessage(message: String?) {
    if (message == null)
      return
    if (message.isNotBlank()) {
      toast(message)
      logd(message)
    }
  }

  private fun observeModel() {
    model.loading.observe { displayLoading(it!!) }
    model.portfolios.observe { displayPortfolios(it ?: emptyList()) }
    model.message.observe { displayMessage(it) }
  }

  private fun <T> LiveData<T>.observe(observe: (T?) -> Unit) =
    observe(this@PortfolioListActivity, { observe(it) })

  private fun setupRecyclerView(recyclerView: RecyclerView) {
    adapter = SimpleItemRecyclerViewAdapter(this, twoPane)
    recyclerView.adapter = adapter
  }
}
