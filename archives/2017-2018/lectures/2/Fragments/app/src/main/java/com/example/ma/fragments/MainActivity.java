package com.example.ma.fragments;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import static com.example.ma.fragments.DetailedFragment.MESSAGE;

public class MainActivity extends AppCompatActivity implements MainFragment.OnSelectionListener {
    public static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            Log.v(TAG, "restore state");
        } else {
            Log.v(TAG, "create layout");
            View mainFragment = findViewById(R.id.main_fragment);
            if (mainFragment != null) {
                MainFragment newFragment = new MainFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(mainFragment.getId(), newFragment);
                fragmentTransaction.commit();
            }
            View detailFragment = findViewById(R.id.detailed_fragment);
            if (detailFragment != null) {
                DetailedFragment newFragment = new DetailedFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(detailFragment.getId(), newFragment);
                fragmentTransaction.commit();
            }

        }
    }

    @Override
    public void onSelect(String message) {
        Log.v(TAG, "onSelect");
        View detailFragment = findViewById(R.id.detailed_fragment);
        if (detailFragment != null) {
            Log.d(TAG, "send message: " + message);
            DetailedFragment newFragment = new DetailedFragment();
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE, message);
            newFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(detailFragment.getId(), newFragment);
            fragmentTransaction.commit();
        }
    }
}
