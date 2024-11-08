package ro.cojocar.sqldelight

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
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

  private lateinit var queries: PlayerQueries

  private lateinit var simpleItemRecyclerViewAdapter: SimpleItemRecyclerViewAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityItemListBinding.inflate(layoutInflater)
    setContentView(binding.root)

    queries = setupDatabase(applicationContext)

    val toolbar = binding.toolbar
    setSupportActionBar(toolbar)
    toolbar.title = title

    binding.fab.setOnClickListener { localView ->
      val nextId = queries.countAll().executeAsOne() + 1
      queries.insertPlayer(
        nextId,
        "Bobby Fischer",
        "I don’t believe in psychology. I believe in good moves"
      )
      val updatedList = chessPlayers()
      simpleItemRecyclerViewAdapter.updateList(updatedList)

      Snackbar.make(localView, "The list was updated!", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }

    val recyclerView: RecyclerView = findViewById(R.id.item_list)
    setupRecyclerView(recyclerView)
  }

  private fun setupRecyclerView(recyclerView: RecyclerView) {

    val values = chessPlayers()
    simpleItemRecyclerViewAdapter =
      SimpleItemRecyclerViewAdapter(values)
    recyclerView.adapter = simpleItemRecyclerViewAdapter
  }

  private fun chessPlayers(): List<ChessPlayer> {
    val values = queries.selectAll().executeAsList()
    return values
  }

  class SimpleItemRecyclerViewAdapter(
    private var values: List<ChessPlayer>
  ) :
    RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
      val item = v.tag as ChessPlayer
      val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.player_number)
      }
      v.context.startActivity(intent)
    }

    fun updateList(newValues: List<ChessPlayer>) {
      val oldSize = values.size
      values = newValues
      notifyItemInserted(oldSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_list_content, parent, false)
      return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val item = values[position]
      holder.idView.text = "${item.full_name} (${item.player_number})"
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