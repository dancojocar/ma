package ro.cojocar.dan.portfolio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_portfolio_detail.*
import kotlinx.android.synthetic.main.portfolio_detail.view.*

/**
 * A fragment representing a single Portfolio detail screen.
 * This fragment is either contained in a [PortfolioListActivity]
 * in two-pane mode (on tablets) or a [PortfolioDetailActivity]
 * on handsets.
 */
class PortfolioDetailFragment : Fragment() {

  private var itemName: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      if (it.containsKey(ARG_ITEM_NAME)) {
        itemName = it.getString(ARG_ITEM_NAME)
        activity?.toolbar_layout?.title = itemName
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val rootView = inflater.inflate(R.layout.portfolio_detail, container, false)

    itemName.let {
      rootView.portfolio_detail.text = it
    }

    return rootView
  }

  companion object {
    /**
     * The fragment argument representing the item  that this fragment
     * represents.
     */
    const val ARG_ITEM_NAME = "item_name"
  }
}
