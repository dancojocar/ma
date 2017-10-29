package com.example.ma.sm;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ma.sm.database.old.SQLSample;
import com.example.ma.sm.database.room.RoomSample;
import com.example.ma.sm.dummy.DummyContentProvider;
import com.example.ma.sm.fragments.PortfolioDetailActivity;
import com.example.ma.sm.fragments.PortfolioFragment;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.preferences.UserSettingsActivity;
import com.example.ma.sm.task.listeners.OnErrorUpdateListener;
import com.example.ma.sm.util.ErrorHandler;

import static com.example.ma.sm.fragments.PortfolioDetailFragment.PORTFOLIO;

public class StockMarketActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    PortfolioFragment.OnListFragmentInteractionListener,
    OnErrorUpdateListener,
    DummyContentProvider {

  private static final String TAG = StockMarketActivity.class.getSimpleName();

  private StockApp app;
  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    app = (StockApp) getApplication();
    app.getManager().setOnErrorUpdateListener(this);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new NewPortfolio(StockMarketActivity.this, app);
      }
    });

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    handler = new ErrorHandler(Looper.getMainLooper(), this);

    Log.v(TAG, "onCreate done");
  }


  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
    Log.v(TAG, "onBackPressed done");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.stock_market, menu);
    Log.v(TAG, "onCreateOptionsMenu done");
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

    Log.v(TAG, "onOptionsItemSelected done");
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    if (id == R.id.nav_one_column) {
      changeColumns(1);
    } else if (id == R.id.nav_two_columns) {
      changeColumns(2);
    } else if (id == R.id.nav_delete) {
      app.getManager().delete();
    } else if (id == R.id.nav_refresh) {
      app.getManager().fetchData();
    } else if (id == R.id.nav_cancel) {
      app.getManager().cancelCall();
    } else if (id == R.id.nav_settings) {
      showPreferences();
    } else if (id == R.id.nav_wifi) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      boolean wifi = prefs.getBoolean("wifi", true);
      Toast.makeText(this, "Wifi settings: " + wifi, Toast.LENGTH_LONG).show();
    } else if (id == R.id.nav_old_db) {
      SQLSample sample = new SQLSample();
      sample.demo(this);
    } else if (id == R.id.nav_new_db) {
      RoomSample sample = new RoomSample();
      sample.demo(this);
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    Log.v(TAG, "onNavigationItemSelected done");
    return true;
  }

  private void showPreferences() {
    Intent intent = new Intent();
    intent.setClass(this, UserSettingsActivity.class);
    startActivity(intent);
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
    Log.v(TAG, "onRestart done");
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Log.v(TAG, "onSaveInstanceState done");
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    Log.v(TAG, "onRestoreInstanceState done");
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.v(TAG, "onStart done");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.v(TAG, "onResume done");
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.v(TAG, "onPause done");
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.v(TAG, "onStop done");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.v(TAG, "onDestroy done");
  }

  @Override
  public void onListFragmentInteraction(Portfolio portfolio) {
    Toast.makeText(getApplication(), "Show portfolio details", Toast.LENGTH_SHORT).show();
    Intent intent = new Intent();
    intent.setClass(this, PortfolioDetailActivity.class);
    intent.putExtra(PORTFOLIO, portfolio);
    startActivity(intent);
  }

  @Override
  public void onError(Exception e) {

    if (handler != null) {
      Message message = handler.obtainMessage(1, e);
      message.sendToTarget();
    }
  }

  @Override
  public void showDummyData() {
    app.getManager().genDummyData();
  }

}
