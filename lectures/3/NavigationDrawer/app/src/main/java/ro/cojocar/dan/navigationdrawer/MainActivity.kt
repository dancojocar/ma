package ro.cojocar.dan.navigationdrawer

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.snackbar

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                logd("Replace with your own action")
            }
            R.id.nav_gallery -> {
                Snackbar.make(drawer_layout,
                    "Replace with your own action",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            R.id.nav_slideshow -> {
                drawer_layout.snackbar("Replace with your own action!")
            }
            R.id.nav_manage -> {
                longToast("Wow, such duration")
            }
            R.id.nav_share -> {
                alert {
                    isCancelable = false
                    lateinit var datePicker: DatePicker
                    customView {
                        verticalLayout {
                            datePicker = datePicker {
                                maxDate = System.currentTimeMillis()
                            }
                        }
                    }
                    yesButton {
                        val parsedDate = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"
                        toast("Selected date: $parsedDate")
                    }
                    noButton { }
                }.show()
            }
            R.id.nav_send -> {
                alert(title = "Fire the missiles", message = "Are you sure?") {
                    yesButton { toast("Replace with your own action!") }
                    noButton { drawer_layout.snackbar("Will send them anyway...") }
                }.show()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
