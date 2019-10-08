package ro.cojocar.dan.listview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val arrayList = (1..100).map { "item: $it" }.toMutableList()
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList)
        mylist.adapter = arrayAdapter
        mylist.setOnItemClickListener { parent, view, position, _ ->
            progress_circular.visibility = View.VISIBLE
            val item = parent.getItemAtPosition(position)
            view.animate().setDuration(1000).alpha(0.0F).withEndAction {
                arrayList.remove(item)
                arrayAdapter.notifyDataSetChanged()
                view.alpha = 1.0F
                progress_circular.visibility = View.GONE
            }
        }
    }
}
