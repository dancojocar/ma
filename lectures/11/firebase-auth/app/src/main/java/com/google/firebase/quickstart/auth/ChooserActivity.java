/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Simple list-based Activity to redirect to one of the other Activities.
 * This Activity does not contain any useful code related to Firebase
 * Authentication. You may want to start with
 * one of the following Files:
 * {@link GoogleSignInActivity}
 * {@link EmailPasswordActivity}
 */
public class ChooserActivity extends AppCompatActivity
    implements AdapterView.OnItemClickListener {

  private static final Class[] CLASSES = new Class[]{
      GoogleSignInActivity.class,
      EmailPasswordActivity.class
  };

  private static final int[] DESCRIPTION_IDS = new int[]{
      R.string.desc_google_sign_in,
      R.string.desc_emailpassword
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chooser);

    // Set up ListView and Adapter
    ListView listView = findViewById(R.id.list_view);

    MyArrayAdapter adapter =
        new MyArrayAdapter(this,
            android.R.layout.simple_list_item_2, CLASSES);
    adapter.setDescriptionIds(DESCRIPTION_IDS);

    listView.setAdapter(adapter);
    listView.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Class clicked = CLASSES[position];
    startActivity(new Intent(this, clicked));
  }

  public static class MyArrayAdapter extends ArrayAdapter<Class> {

    private Context mContext;
    private Class[] mClasses;
    private int[] mDescriptionIds;

    MyArrayAdapter(Context context, int resource, Class[] objects) {
      super(context, resource, objects);
      mContext = context;
      mClasses = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView,
                        @NonNull ViewGroup parent) {
      View view = convertView;

      if (convertView == null) {
        LayoutInflater inflater =
            (LayoutInflater) mContext.
                getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
          view = inflater.
              inflate(android.R.layout.simple_list_item_2, null);
        }
      }

      TextView tv1 = view.findViewById(android.R.id.text1);
      tv1.setText(mClasses[position].getSimpleName());
      TextView tv2 = view.findViewById(android.R.id.text2);
      tv2.setText(mDescriptionIds[position]);

      return view;
    }

    void setDescriptionIds(int[] descriptionIds) {
      mDescriptionIds = descriptionIds;
    }
  }
}
