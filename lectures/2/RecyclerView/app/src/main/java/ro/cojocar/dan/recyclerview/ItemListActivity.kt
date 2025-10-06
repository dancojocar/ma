package ro.cojocar.dan.recyclerview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ro.cojocar.dan.recyclerview.databinding.ActivityItemListBinding
import ro.cojocar.dan.recyclerview.databinding.ItemListContentBinding
import ro.cojocar.dan.recyclerview.dummy.DummyContent

class ItemListActivity : AppCompatActivity() {

  private lateinit var binding: ActivityItemListBinding
  private var twoPane: Boolean = false
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityItemListBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)
    binding.toolbar.title = title

    binding.fab.setOnClickListener { view ->
      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show()
    }

    val itemListBinding = binding.itemListLayout
    if (itemListBinding.itemDetailContainer != null) {
      twoPane = true
    }

    val recyclerView: RecyclerView = itemListBinding.itemListRecyclerView
    setupRecyclerView(recyclerView)
  }

  private fun setupRecyclerView(recyclerView: RecyclerView) {
    recyclerView.adapter = SimpleItemRecyclerViewAdapter(DummyContent.ITEMS, startForResult, twoPane, supportFragmentManager)
  }

  private val startForResult = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result: ActivityResult ->
    if (result.resultCode == Activity.RESULT_OK) {
      val intent = result.data
      val returnedResult = intent?.getStringExtra("resultKey")
      Toast.makeText(this, "Received: $returnedResult", Toast.LENGTH_SHORT).show()
    }
  }

  class SimpleItemRecyclerViewAdapter(
    private val values: List<DummyContent.DummyItem>,
    private val launcher: ActivityResultLauncher<Intent>,
    private val twoPane: Boolean,
    private val supportFragmentManager: androidx.fragment.app.FragmentManager
  ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
      val item = v.tag as DummyContent.DummyItem
      if (twoPane) {
        val fragment = ItemDetailFragment().apply {
          arguments = Bundle().apply {
            putString(ItemDetailFragment.ARG_ITEM_ID, item.id)
          }
        }
        supportFragmentManager.beginTransaction()
          .replace(R.id.item_detail_container, fragment)
          .commit()
      } else {
        val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
          putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id)
        }
        launcher.launch(intent)
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val binding = ItemListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
      return ViewHolder(binding)
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

    inner class ViewHolder(binding: ItemListContentBinding) : RecyclerView.ViewHolder(binding.root) {
      val idView: TextView = binding.idText
      val contentView: TextView = binding.content
    }
  }
}
