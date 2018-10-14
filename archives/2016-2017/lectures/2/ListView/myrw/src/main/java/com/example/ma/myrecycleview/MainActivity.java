package com.example.ma.myrecycleview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MyAdapter adapter;
    private ArrayList<Photo> photoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(photoList);
        recyclerView.setAdapter(adapter);
        Log.v("main", "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("main", "onStart");

        if (photoList.size() == 0) {
            photoList.add(new Photo("https://upload.wikimedia.org/wikipedia/en/5/53/The_first_Google_Doodle.png", "Just now :)", "The google logo"));
            photoList.add(new Photo("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a6/Triton_moon_mosaic_Voyager_2_%28large%29.jpg/320px-Triton_moon_mosaic_Voyager_2_%28large%29.jpg", "Just now2 :)", "Triton"));
            photoList.add(new Photo("http://www.ubbcluj.ro/img/logo_UBB_ro.png", "Yesterday", "The ubb logo"));
            photoList.add(new Photo("http://www.cs.ubbcluj.ro/wp-content/themes/CSUBB/images/logo.png", "A long time ago", "The cs logo"));
            adapter.notifyItemInserted(photoList.size());
            Log.v("main", "added some photos: " + photoList.size());
        }
    }
}
