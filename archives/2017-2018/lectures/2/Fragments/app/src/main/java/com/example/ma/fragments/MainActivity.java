package com.example.ma.fragments;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements MainFragment.OnSelectionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            Log.v("main", "restore state");
        } else {
            Log.v("main", "create layout");
            View mainFragment = findViewById(R.id.main_fragment);
            if (mainFragment!=null){
                MainFragment newFragment=new MainFragment();
                FragmentTransaction fragmentTransaction=getFragmentManager().beginTransaction();
                fragmentTransaction.replace(mainFragment.getId(),newFragment);
                fragmentTransaction.commit();
            }
            View detailFragment = findViewById(R.id.detailed_fragment);
            if (detailFragment!=null){
                DetailedFragment newFragment=new DetailedFragment();
                FragmentTransaction fragmentTransaction=getFragmentManager().beginTransaction();
                fragmentTransaction.replace(detailFragment.getId(),newFragment);
                fragmentTransaction.commit();
            }

        }
    }

    @Override
    public void onSelect(String message) {
        Log.v("main","onSelect");
        View detailFragment = findViewById(R.id.detailed_fragment);
        if (detailFragment!=null){
            Log.v("main","send message: "+message);
            DetailedFragment newFragment=new DetailedFragment();
            Bundle bundle=new Bundle();
            bundle.putString("message",message);
            newFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction=getFragmentManager().beginTransaction();
            fragmentTransaction.replace(detailFragment.getId(),newFragment);
            fragmentTransaction.commit();
        }
    }
}
