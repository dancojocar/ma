/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.ubbcluj.cs.ds.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;

import ro.ubbcluj.cs.ds.MainActivityViewModel;
import ro.ubbcluj.cs.ds.R;
import ro.ubbcluj.cs.ds.TrivialDriveApplication;
import ro.ubbcluj.cs.ds.databinding.ActivityMainBinding;

/*
 * MainActivity here exists as a container for the fragments that display the various bits of UI,
 * as well as the CoordinatorLayout/SnackBar implementation.
 */
public class MainActivity extends AppCompatActivity{
    private MainActivityViewModel mainActivityViewModel;
    private ActivityMainBinding activityMainBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MainActivity", "onCreate");
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // Setup toolbar with nav controller
        NavController navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = activityMainBinding.toolbar;
        setSupportActionBar(toolbar);
        NavigationUI.setupWithNavController(
                toolbar, navController, appBarConfiguration);

        MainActivityViewModel.MainActivityViewModelFactory mainActivityViewModelFactory = new
                MainActivityViewModel.MainActivityViewModelFactory(
                ((TrivialDriveApplication)getApplication()).getAppContainer().
                        getTrivialDriveRepository());
        mainActivityViewModel = new ViewModelProvider(this, mainActivityViewModelFactory)
                .get(MainActivityViewModel.class);

        // Create our Activity ViewModel, which exists to handle global Snackbar messages
        mainActivityViewModel.getMessages().observe(this, resourceId -> {
            Snackbar snackbar = Snackbar.make(activityMainBinding.mainLayout,getString(resourceId),Snackbar.LENGTH_SHORT);
            snackbar.show();
        });
        // Allows billing to refresh purchases during onResume
        getLifecycle().addObserver(mainActivityViewModel.getBillingLifecycleObserver());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        // we could nicely disable this when we don't have a premium purchase by observing
        // the LiveData for the SKU_GAS, but it's just there for testing
        if (item.getItemId() == R.id.menu_consume_premium) {
            mainActivityViewModel.debugConsumePremium();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PublicKeyNotSetDialog extends DialogFragment {
        static final String DIALOG_TAG = "PublicKeyNotSetDialog";
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.alert_error_title_encoded_public_key_not_set)
                    .setMessage(
                            R.string.alert_error_message_encoded_public_key_not_set)
                    .setPositiveButton(getString(android.R.string.ok),
                            (dialog, which) -> {
                            })
                    .create();
        }
    }
}
