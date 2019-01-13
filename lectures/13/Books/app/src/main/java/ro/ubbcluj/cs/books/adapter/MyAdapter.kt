package ro.ubbcluj.cs.books.adapter

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import ro.ubbcluj.cs.books.EventDetailActivity
import ro.ubbcluj.cs.books.EventDetailFragment
import ro.ubbcluj.cs.books.books.R
import ro.ubbcluj.cs.books.domain.Book

class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

  private var mValues = mutableListOf<Book>()

  fun setData(books: MutableList<Book>) {
    mValues.clear()
    mValues.addAll(books)
    notifyDataSetChanged()
  }

  fun clear() {
    mValues.clear()
    notifyDataSetChanged()
  }


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.book_list_content, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.mItem = mValues[position]
    holder.mIdView.text = mValues[position].id.toString()
    holder.mContentView.text = mValues[position].title

    holder.mView.setOnClickListener { v ->
      val context = v.context
      val intent = Intent(context, EventDetailActivity::class.java)
      intent.putExtra(EventDetailFragment.ARG_ITEM_ID, holder.mItem!!.id.toString())

      context.startActivity(intent)
    }
  }

  override fun getItemCount(): Int {
    return mValues.size
  }

  inner class ViewHolder internal constructor(internal val mView: View) : RecyclerView.ViewHolder(mView) {
    internal val mIdView: TextView = mView.findViewById(R.id.id)
    internal val mContentView: TextView = mView.findViewById(R.id.content)
    internal var mItem: Book? = null

    override fun toString(): String {
      return "${super.toString()} '${mContentView.text}'"
    }
  }
}
