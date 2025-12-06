package ro.cojocar.dan.vectordrawable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContentView(R.layout.activity_main)
    val listView = findViewById<ListView>(android.R.id.list)

    ViewCompat.setOnApplyWindowInsetsListener(listView) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    val demos = listOf(
      Demo(this, ExampleActivity::class.java, R.string.example_from_documentation),
      Demo(this, RotateActivity::class.java, R.string.clock),
      Demo(this, PathMorphActivity::class.java, R.string.smiling_face),
      Demo(this, FillInHeartActivity::class.java, R.string.fill_in_heart)
    )

    val adapter = ArrayAdapter(
      this,
      android.R.layout.simple_list_item_1,
      demos
    )
    listView.adapter = adapter

    listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
      val demo = demos[position]
      startActivity(Intent(this@MainActivity, demo.activityClass))
    }
  }

  class Demo(context: Context, val activityClass: Class<*>, titleId: Int) {
    private val title: String = context.getString(titleId)

    override fun toString(): String {
      return this.title
    }
  }
}
