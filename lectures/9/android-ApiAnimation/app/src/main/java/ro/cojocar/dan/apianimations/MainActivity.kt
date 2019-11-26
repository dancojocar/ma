package ro.cojocar.dan.apianimations

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import java.util.*

class MainActivity : ListActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val intent = intent
    var path: String? = intent.getStringExtra(PATH)

    if (path == null) {
      path = ""
    }

    listAdapter = SimpleAdapter(
        this, getData(path),
        android.R.layout.simple_list_item_1, arrayOf("title"),
        intArrayOf(android.R.id.text1)
    )
    listView.isTextFilterEnabled = true
  }

  private fun getData(prefix: String): List<Map<String, Any>> {
    val myData = ArrayList<Map<String, Any>>()

    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE)

    val pm = packageManager
    val list = pm.queryIntentActivities(mainIntent, 0) ?: return myData

    val prefixPath: Array<String>?
    var prefixWithSlash = prefix

    if (prefix == "") {
      prefixPath = null
    } else {
      prefixPath = prefix.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      prefixWithSlash = "$prefix/"
    }

    val len = list.size

    val entries = HashMap<String, Boolean>()

    for (i in 0 until len) {
      val info = list[i]
      val labelSeq = info.loadLabel(pm)
      val label = labelSeq?.toString() ?: info.activityInfo.name

      if (prefixWithSlash.isEmpty() || label.startsWith(prefixWithSlash)) {

        val labelPath = label.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val nextLabel = if (prefixPath == null)
          labelPath[0]
        else
          labelPath[prefixPath.size]

        if (prefixPath?.size ?: 0 == labelPath.size - 1) {
          addItem(
              myData, nextLabel, activityIntent(
              info.activityInfo.applicationInfo.packageName,
              info.activityInfo.name
          )
          )
        } else {
          if (entries[nextLabel] == null) {
            addItem(
                myData, nextLabel,
                browseIntent(
                    if (prefix == "")
                      nextLabel
                    else
                      "$prefix/$nextLabel"
                )
            )
            entries[nextLabel] = true
          }
        }
      }
    }

    Collections.sort(myData, sDisplayNameComparator)

    return myData
  }

  private fun activityIntent(pkg: String, componentName: String): Intent {
    val result = Intent()
    result.setClassName(pkg, componentName)
    return result
  }

  private fun browseIntent(path: String): Intent {
    val result = Intent()
    result.setClass(this, MainActivity::class.java)
    result.putExtra(PATH, path)
    return result
  }

  private fun addItem(data: MutableList<Map<String, Any>>, name: String, intent: Intent) {
    val temp = HashMap<String, Any>()
    temp["id"] = name.substring(0, name.indexOf("."))
    temp["title"] = name
    temp["intent"] = intent
    data.add(temp)
  }

  override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val map = l.getItemAtPosition(position) as Map<*, *>

    val intent = map["intent"] as Intent?
    startActivity(intent)
  }

  companion object {
    private const val PATH = "ro.cojocar.dan.apianimations.apis.Path"

    private val sDisplayNameComparator = object : Comparator<Map<String, Any>> {
      override fun compare(map1: Map<String, Any>, map2: Map<String, Any>): Int {
        return map1["id"].toString().toInt().compareTo(map2["id"].toString().toInt())
      }
    }
  }
}