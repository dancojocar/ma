package com.example.ma.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by dan on 10/10/16.
 */
public class DetailedFragment extends Fragment {

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v("detailed", "onCreateView");
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.detailed_fragment, container, false);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (rootView == null) {
            rootView=getActivity().findViewById(R.id.detailed_fragment);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String message="init";
        if (getArguments() != null) {
            message = getArguments().getString("message");
            Log.v("detailed", "received message: " + message);
        }
        Log.v("detailed", "onActivityCreated message: "+message);
        LinearLayout ll = (LinearLayout) rootView;
        for (int i = 0; i < 5; i++) {
            TextView tv = new TextView(getActivity());
            tv.setText(message);
            ll.addView(tv);
        }
    }
}
