package ro.cojocar.dan.portfolio.adapters

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.portfolio_list_content.view.*
import ro.cojocar.dan.portfolio.*
import ro.cojocar.dan.portfolio.domain.Portfolio

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
            putString(PortfolioDetailFragment.ARG_ITEM_NAME, item.name)
          }
        }
        parentActivity.supportFragmentManager
          .beginTransaction()
          .replace(R.id.portfolio_detail_container, fragment)
          .commit()
      } else {
        val intent = Intent(v.context, PortfolioDetailActivity::class.java).apply {
          putExtra(PortfolioDetailFragment.ARG_ITEM_NAME, item.name)
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
