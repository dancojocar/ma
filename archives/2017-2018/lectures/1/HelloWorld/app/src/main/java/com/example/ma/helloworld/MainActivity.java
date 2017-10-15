package com.example.ma.helloworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String TEST = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TEST,"onCreate");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TEST,"onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TEST,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TEST,"onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TEST,"onDestroy");
    }

    public void press(View view) {
        Log.v(TEST,"press");

    }
}
