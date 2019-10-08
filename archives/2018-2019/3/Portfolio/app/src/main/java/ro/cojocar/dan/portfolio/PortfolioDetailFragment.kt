package ro.cojocar.dan.portfolio

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_portfolio_detail.*
import kotlinx.android.synthetic.main.portfolio_detail.view.*
import ro.cojocar.dan.portfolio.domain.Portfolio
import ro.cojocar.dan.portfolio.dummy.DummyContent

/**
 * A fragment representing a single Portfolio detail screen.
 * This fragment is either contained in a [PortfolioListActivity]
 * in two-pane mode (on tablets) or a [PortfolioDetailActivity]
 * on handsets.
 */
class PortfolioDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: Portfolio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = DummyContent.portfoliosMap[it.getLong(ARG_ITEM_ID)]
                activity?.toolbar_layout?.title = item?.name
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.portfolio_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.portfolio_detail.text = it.name
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
