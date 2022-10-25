package ro.cojocar.dan.preferences

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (savedInstanceState == null) {
      supportFragmentManager
          .beginTransaction()
          .replace(R.id.settings, SettingsFragment())
          .commit()
    } else {
      title = savedInstanceState.getCharSequence(TITLE_TAG)
    }
    supportFragmentManager.addOnBackStackChangedListener {
      if (supportFragmentManager.backStackEntryCount == 0) {
        setTitle(R.string.title)
      }
    }
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // Save current activity title so we can set it again after a configuration change
    outState.putCharSequence(TITLE_TAG, title)
  }

  override fun onSupportNavigateUp(): Boolean {
    if (supportFragmentManager.popBackStackImmediate()) {
      return true
    }
    return super.onSupportNavigateUp()
  }

  override fun onPreferenceStartFragment(
      caller: PreferenceFragmentCompat,
      pref: Preference
  ): Boolean {
    // Instantiate the new Fragment
    val args = pref.extras
    val fragment = supportFragmentManager.fragmentFactory.instantiate(
        classLoader,
        pref.fragment!!
    ).apply {
      arguments = args
      setTargetFragment(caller, 0)
    }
    // Replace the existing Fragment with the new Fragment
    supportFragmentManager.beginTransaction()
        .replace(R.id.settings, fragment)
        .addToBackStack(null)
        .commit()
    title = pref.title
    return true
  }

  /**
   * The root preference fragment that displays preferences that link to the other preference
   * fragments below.
   */
  class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.root, rootKey)
    }
  }

  /**
   * A preference fragment that demonstrates commonly used preference attributes.
   */
  class BasicPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.basic_preferences, rootKey)
    }
  }

  /**
   * A preference fragment that demonstrates preferences which contain dynamic widgets.
   */
  class WidgetPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.widgets, rootKey)
    }
  }

  /**
   * A preference fragment that demonstrates preferences that launch a dialog when tapped.
   */
  class DialogPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.dialogs, rootKey)
    }
  }

  /**
   * A preference fragment that demonstrates more advanced attributes and functionality.
   */
  class AdvancedPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.advanced, rootKey)
    }
  }
}