package com.example.ma.fragments;

import android.app.Activity;
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

import static android.R.attr.fragment;

/**
 * Created by dan on 10/10/16.
 */

public class MainFragment extends Fragment {

    OnSelectionListener listener;

    public interface OnSelectionListener{
        public void onSelect(String message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener=(OnSelectionListener)context;
            Log.v("mainFrag","listener attached");
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implement OnSelectionListener");
        }
    }

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_fragment, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayout ll=(LinearLayout) rootView;
        for (int i=0;i<100;i++){
            LinearLayout hl=new LinearLayout(getActivity());
            hl.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv=new TextView(getActivity());
            tv.setText("Test: "+i);
            hl.addView(tv);
            Button button=new Button(getActivity());
            button.setText("OK: "+i);
            final int count=i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onSelect("Button: "+count+" was pressed!");
                }
            });
            hl.addView(button);
            ll.addView(hl);

        }
    }
}
