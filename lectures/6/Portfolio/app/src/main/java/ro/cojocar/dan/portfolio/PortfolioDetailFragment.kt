package ro.cojocar.dan.portfolio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ro.cojocar.dan.portfolio.databinding.PortfolioDetailBinding

/**
 * A fragment representing a single Portfolio detail screen.
 * This fragment is either contained in a [PortfolioListActivity]
 * in two-pane mode (on tablets) or a [PortfolioDetailActivity]
 * on handsets.
 */
class PortfolioDetailFragment : Fragment() {

  private var itemName: String? = null
  private var _binding: PortfolioDetailBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      if (it.containsKey(ARG_ITEM_NAME)) {
        itemName = it.getString(ARG_ITEM_NAME)
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = PortfolioDetailBinding.inflate(inflater, container, false)
    val view = binding.root
    itemName.let {
      binding.portfolioDetail.text = it
    }
    return view
  }

  companion object {
    /**
     * The fragment argument representing the item  that this fragment
     * represents.
     */
    const val ARG_ITEM_NAME = "item_name"
  }
}
