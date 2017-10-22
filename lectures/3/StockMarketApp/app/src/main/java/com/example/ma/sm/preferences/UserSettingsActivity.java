package com.example.ma.sm.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ma.sm.R;

import java.util.List;

public class UserSettingsActivity extends PreferenceActivity {

  private static final String TAG = UserSettingsActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Add a button to the header list.
    if (hasHeaders()) {
      Button button = new Button(this);
      button.setText(R.string.preferenceSomeAction);
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UserSettingsActivity.this);
          Log.v(TAG, "wifi setting: " + prefs.getBoolean("wifi", true));
        }
      });
      setListFooter(button);
    }
  }

  /**
   * Populate the activity with the top-level headers.
   */
  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.preference_headers, target);
  }

  @Override
  protected boolean isValidFragment(String fragmentName) {
    return true;
  }

  /**
   * This fragment shows the preferences for the first header.
   */
  public static class DemoSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Make sure default values are applied.  In a real app, you would
      // want this in a shared function that is used to retrieve the
      // SharedPreferences wherever they are needed.
      PreferenceManager.setDefaultValues(getActivity(),
          R.xml.advanced_preferences, false);

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.fragmented_preferences);
    }
  }

  /**
   * This fragment contains a second-level set of preference that you
   * can get to by tapping an item in the first preferences fragment.
   */
  public static class DemoSettingsFragmentInner extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Can retrieve arguments from preference XML.
      Log.i(TAG, "Arguments: " + getArguments());

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.fragmented_preferences_inner);
    }
  }

  /**
   * This fragment shows the preferences for the second header.
   */
  public static class WifiSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Can retrieve arguments from headers XML.
      Log.i(TAG, "Arguments: " + getArguments());

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.wifi_preference_dependencies);
    }
  }
}
