package com.example.ma.sm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ma.sm.database.SQLSample;
import com.example.ma.sm.dialog.NewPortfolio;
import com.example.ma.sm.files.ListFilesActivity;
import com.example.ma.sm.fragments.BaseActivity;
import com.example.ma.sm.fragments.PortfolioDetailActivity;
import com.example.ma.sm.fragments.PortfolioFragment;
import com.example.ma.sm.jobs.TestJobActivity;
import com.example.ma.sm.location.LocationDemo;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.oauth.GoogleSheetAPI;
import com.example.ma.sm.preferences.UserSettingsActivity;
import com.example.ma.sm.rx.RxDemo;
import com.example.ma.sm.sensors.AccelerationDemo;
import com.example.ma.sm.sensors.AccelerometerPlayActivity;
import com.example.ma.sm.sensors.PressureDemo;
import com.example.ma.sm.service.ExampleService;
import com.example.ma.sm.task.listeners.OnErrorUpdateListener;
import com.example.ma.sm.util.Constants;
import com.example.ma.sm.util.ErrorHandler;

import timber.log.Timber;

public class StockMarketActivity extends BaseActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    PortfolioFragment.OnListFragmentInteractionListener,
    OnErrorUpdateListener {

  private StockApp app = StockApp.get();
  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    app.getManager().setOnErrorUpdateListener(this);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new NewPortfolio(StockMarketActivity.this, app);
      }
    });

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    handler = new ErrorHandler(Looper.getMainLooper(), this);
    Timber.v("onCreate done");
  }


  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
    Timber.v("onBackPressed done");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.stock_market, menu);
    Timber.v("onCreateOptionsMenu done");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      showPreferences();
      return true;
    }

    Timber.v("onOptionsItemSelected done");
    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // Handle navigation view item clicks here.
    int id = item.getItemId();
    if (id == R.id.nav_one_column) {
      changeColumns(1);
    } else if (id == R.id.nav_two_columns) {
      changeColumns(2);
    } else if (id == R.id.nav_delete) {
      app.getManager().delete();
    } else if (id == R.id.nav_deleteToken) {
      app.getManager().deleteTokens();
    } else if (id == R.id.nav_refresh) {
      app.getManager().fetchData();
    } else if (id == R.id.nav_cancel) {
      app.getManager().cancelCall();
    } else if (id == R.id.nav_login) {
      app.getManager().login("test", "test1");
    } else if (id == R.id.nav_settings) {
      showPreferences();
    } else if (id == R.id.nav_wifi) {
      Timber.v("wifi setting: %b", prefs.getBoolean(Constants.WIFI, true));
    } else if (id == R.id.nav_sql) {
      new SQLSample().demo(this);
    } else if (id == R.id.nav_google) {
      openAuthActivity();
    } else if (id == R.id.nav_google_clear) {
      prefs.edit().remove(GoogleSheetAPI.PREF_ACCOUNT_NAME).apply();
    } else if (id == R.id.nav_files) {
      gotoFiles();
    } else if (id == R.id.nav_ws) {
      boolean wifi = prefs.getBoolean(Constants.WIFI, true);
      if (wifi)
        connectToWS();
    } else if (id == R.id.nav_ws_close) {
      boolean wifi = prefs.getBoolean(Constants.WIFI, true);
      if (wifi)
        disconnectFromWS();
    } else if (id == R.id.nav_crash) {
      throw new RuntimeException("crash");
    } else if (id == R.id.nav_rx) {
      new RxDemo().start();
    } else if (id == R.id.nav_location) {
      gotoActivity(LocationDemo.class);
    } else if (id == R.id.nav_test_job) {
      gotoActivity(TestJobActivity.class);
    } else if (id == R.id.nav_acceleration) {
      gotoActivity(AccelerationDemo.class);
    } else if (id == R.id.nav_pressure) {
      gotoActivity(PressureDemo.class);
    } else if (id == R.id.nav_service) {
      Intent service = new Intent(this, ExampleService.class);
      startService(service);
    } else if (id == R.id.nav_service_stop) {
      Intent service = new Intent(this, ExampleService.class);
      stopService(service);
    } else if (id == R.id.nav_play_demo) {
      gotoActivity(AccelerometerPlayActivity.class);
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    Timber.v("onNavigationItemSelected done");
    return true;
  }

  private void disconnectFromWS() {
    app.getManager().disconnectFromWS();
  }

  private void connectToWS() {
    app.getManager().connectToWS();
  }

  private void gotoFiles() {
    gotoActivity(ListFilesActivity.class);
  }

  private void gotoActivity(Class<? extends Activity> activity) {
    Intent intent = new Intent();
    intent.setClass(this, activity);
    startActivity(intent);
  }

  private void openAuthActivity() {
    gotoActivity(GoogleSheetAPI.class);
  }

  private void showPreferences() {
    gotoActivity(UserSettingsActivity.class);
  }

  private void changeColumns(int columns) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    ft.replace(R.id.fragment, PortfolioFragment.newInstance(columns));
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.commit();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    Timber.v("onRestart done");
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Timber.v("onSaveInstanceState done");
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    Timber.v("onRestoreInstanceState done");
  }

  @Override
  protected void onStart() {
    super.onStart();
    Timber.v("onStart done");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Timber.v("onResume done");
  }

  @Override
  protected void onPause() {
    super.onPause();
    Timber.v("onPause done");
  }

  @Override
  protected void onStop() {
    super.onStop();
    Timber.v("onStop done");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Timber.v("onDestroy done");
  }

  @Override
  public void onListFragmentInteraction(Portfolio portfolio) {
    Toast.makeText(getApplication(), "Show portfolio details", Toast.LENGTH_SHORT).show();
    Intent intent = new Intent();
    intent.setClass(this, PortfolioDetailActivity.class);
    intent.putExtra("portfolioId", portfolio.getId());
    startActivity(intent);
  }

  @Override
  public void onError(Exception e) {

    if (handler != null) {
      Message message = handler.obtainMessage(1, e);
      message.sendToTarget();
    }
  }

}
