package com.example.ma.sm.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.example.ma.sm.R;
import com.example.ma.sm.util.Constants;

import java.util.List;

import timber.log.Timber;

public class UserSettingsActivity extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Add a button to the header list.
    if (hasHeaders()) {
      Button button = new Button(this);
      button.setText("Some action");
      button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UserSettingsActivity.this);
          Timber.v("wifi setting: %s", prefs.getBoolean(Constants.WIFI, true));
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
  public static class Prefs1Fragment extends PreferenceFragment {
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
  public static class Prefs1FragmentInner extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Can retrieve arguments from preference XML.
      Timber.i("Arguments: %s", getArguments());

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.fragmented_preferences_inner);
    }
  }

  /**
   * This fragment shows the preferences for the second header.
   */
  public static class Prefs2Fragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Can retrieve arguments from headers XML.
      Timber.i("Arguments: %s", getArguments());

      // Load the preferences from an XML resource
      addPreferencesFromResource(R.xml.preference_dependencies);
    }
  }
}
