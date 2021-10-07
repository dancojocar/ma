package ro.cojocar.dan.recyclerview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import ro.cojocar.dan.recyclerview.dummy.DummyContent

class ItemListActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_item_list)

    setSupportActionBar(toolbar)
    toolbar.title = title

    fab.setOnClickListener { view ->
      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
          .setAction("Action", null).show()
    }

    setupRecyclerView(item_list)
  }

  private fun setupRecyclerView(recyclerView: RecyclerView) {
    recyclerView.adapter = SimpleItemRecyclerViewAdapter(DummyContent.ITEMS)
  }

  class SimpleItemRecyclerViewAdapter(
      private val values: List<DummyContent.DummyItem>
  ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
      val item = v.tag as DummyContent.DummyItem
      val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id)
      }
      v.context.startActivity(intent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
          .inflate(R.layout.item_list_content, parent, false)
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = values[position]
      holder.idView.text = item.id
      holder.contentView.text = item.content

      with(holder.itemView) {
        tag = item
        setOnClickListener(onClickListener)
      }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val idView: TextView = view.id_text
      val contentView: TextView = view.content
    }
  }
}
