package ro.cojocar.sqldelight

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.sqldelight.android.AndroidSqliteDriver
import ro.cojocar.sqldelight.databinding.ActivityItemListBinding

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {
  private lateinit var binding: ActivityItemListBinding

  /**
   * Whether or not the activity is in two-pane mode, i.e. running on a tablet
   * device.
   */
  private var twoPane: Boolean = false

  private lateinit var queries: PlayerQueries

  private lateinit var simpleItemRecyclerViewAdapter: SimpleItemRecyclerViewAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityItemListBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    val androidSqlDriver = AndroidSqliteDriver(
      schema = Database.Schema,
      context = applicationContext,
      name = "items.db"
    )

    queries = Database(androidSqlDriver).playerQueries

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
    toolbar.title = title


    binding.fab.setOnClickListener { view ->
      queries.insertPlayer(
        1,
        "Bobby Fischer",
        "I don’t believe in psychology. I believe in good moves"
      )
      simpleItemRecyclerViewAdapter.notifyDataSetChanged()
      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }

    if (findViewById<NestedScrollView>(R.id.item_detail_container) != null) {
      // The detail container view will be present only in the
      // large-screen layouts (res/values-w900dp).
      // If this view is present, then the
      // activity should be in two-pane mode.
      twoPane = true
    }

    val recyclerView: RecyclerView = findViewById(R.id.item_list)
    setupRecyclerView(recyclerView)
  }

  private fun setupRecyclerView(recyclerView: RecyclerView) {

    simpleItemRecyclerViewAdapter =
      SimpleItemRecyclerViewAdapter(this, queries.selectAll().executeAsList(), twoPane)
    recyclerView.adapter = simpleItemRecyclerViewAdapter
  }

  class SimpleItemRecyclerViewAdapter(
    private val parentActivity: ItemListActivity,
    private val values: List<ChessPlayer>,
    private val twoPane: Boolean
  ) :
    RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
      onClickListener = View.OnClickListener { v ->
        val item = v.tag as ChessPlayer
        if (twoPane) {
          val fragment = ItemDetailFragment().apply {
            arguments = Bundle().apply {
              putLong(ItemDetailFragment.ARG_ITEM_ID, item.player_number)
            }
          }
          parentActivity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.item_detail_container, fragment)
            .commit()
        } else {
          val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
            putExtra(ItemDetailFragment.ARG_ITEM_ID, item.player_number)
          }
          v.context.startActivity(intent)
        }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_list_content, parent, false)
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = values[position]
      holder.idView.text = item.full_name
      holder.contentView.text = item.quotes

      with(holder.itemView) {
        tag = item
        setOnClickListener(onClickListener)
      }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val idView: TextView = view.findViewById(R.id.id_text)
      val contentView: TextView = view.findViewById(R.id.content)
    }
  }
}