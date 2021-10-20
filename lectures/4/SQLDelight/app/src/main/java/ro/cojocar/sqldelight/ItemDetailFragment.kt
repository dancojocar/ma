package ro.cojocar.sqldelight

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.squareup.sqldelight.android.AndroidSqliteDriver

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

  /**
   * The dummy content this fragment is presenting.
   */
  private var item: ChessPlayer? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val androidSqlDriver = AndroidSqliteDriver(
      schema = Database.Schema,
      context = context!!,
      name = "players.db"
    )

    val queries = Database(androidSqlDriver).playerQueries
    arguments?.let {
      if (it.containsKey(ARG_ITEM_ID)) {
        val selectById = queries.selectById(it.getLong(ARG_ITEM_ID))
        item = selectById.executeAsOneOrNull()
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title =
          item?.full_name
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val rootView = inflater.inflate(R.layout.item_detail, container, false)

    // Show the dummy content as text in a TextView.
    item?.let {
      rootView.findViewById<TextView>(R.id.item_detail).text = it.quotes
    }

    return rootView
  }

  companion object {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    const val ARG_ITEM_ID = "item_id"
  }
}