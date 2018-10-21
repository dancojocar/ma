package ro.cojocar.dan.portfolio

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_portfolio_list.*
import kotlinx.android.synthetic.main.portfolio_list.*
import kotlinx.android.synthetic.main.portfolio_list_content.view.*
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

    private lateinit var model: MainModel
    private lateinit var adapter: SimpleItemRecyclerViewAdapter

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (portfolio_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        setupRecyclerView(portfolio_list)
        model = ViewModelProviders.of(this).get(MainModel::class.java)
        model.loading.observe { displayLoading(it == true) }
        model.portfolios.observe { displayPortfolios(it ?: emptyList()) }
        model.message.observe { displayMessage(it ?: "") }
    }

    private fun displayLoading(loading: Boolean) {
        progress.visibility = if (loading) VISIBLE else INVISIBLE
    }

    private fun displayPortfolios(portfolios: List<Portfolio>) {
        adapter.portfolios = portfolios
    }

    private fun displayMessage(message: String) {
        if (message.isNotBlank()) {
            toast(message)
            logd(message)
        }
    }

    private fun <T> LiveData<T>.observe(observe: (T?) -> Unit) =
        observe(this@PortfolioListActivity, Observer { observe(it) })

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = SimpleItemRecyclerViewAdapter(this, twoPane)
        recyclerView.adapter = adapter
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: PortfolioListActivity,
        private val twoPane: Boolean
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        var portfolios = emptyList<Portfolio>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        private val onClickListener: View.OnClickListener


        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Portfolio
                if (twoPane) {
                    val fragment = PortfolioDetailFragment().apply {
                        arguments = Bundle().apply {
                            putLong(PortfolioDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.portfolio_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, PortfolioDetailActivity::class.java).apply {
                        putExtra(PortfolioDetailFragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.portfolio_list_content))


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = portfolios[position]
            holder.contentView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = portfolios.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val contentView: TextView = view.content
        }
    }
}
