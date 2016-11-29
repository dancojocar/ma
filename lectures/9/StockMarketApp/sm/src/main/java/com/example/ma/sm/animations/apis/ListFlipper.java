/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ma.sm.animations.apis;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.ma.sm.R;

public class ListFlipper extends Activity {

  private static final int DURATION = 1500;

  private static final String[] LIST_STRINGS_EN = new String[]{
      "One",
      "Two",
      "Three",
      "Four",
      "Five",
      "Six"
  };
  private static final String[] LIST_STRINGS_RO = new String[]{
      "Unu",
      "Doi",
      "Trei",
      "Patru",
      "Cinci",
      "Sase"
  };

  ListView mEnglishList;
  ListView mRomanianList;
  private Interpolator accelerator = new AccelerateInterpolator();
  private Interpolator decelerator = new DecelerateInterpolator();

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.rotating_list);
    //FrameLayout container = (LinearLayout) findViewById(R.id.container);
    mEnglishList = (ListView) findViewById(R.id.list_en);
    mRomanianList = (ListView) findViewById(R.id.list_ro);

    // Prepare the ListView
    final ArrayAdapter<String> adapterEn = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, LIST_STRINGS_EN);
    // Prepare the ListView
    final ArrayAdapter<String> adapterFr = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, LIST_STRINGS_RO);

    mEnglishList.setAdapter(adapterEn);
    mRomanianList.setAdapter(adapterFr);
    mRomanianList.setRotationY(-90f);

    Button starter = (Button) findViewById(R.id.button);
    starter.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        flip();
      }
    });
  }

  private void flip() {
    final ListView visibleList;
    final ListView invisibleList;
    if (mEnglishList.getVisibility() == View.GONE) {
      visibleList = mRomanianList;
      invisibleList = mEnglishList;
    } else {
      invisibleList = mRomanianList;
      visibleList = mEnglishList;
    }
    ObjectAnimator visibleToInvisible = ObjectAnimator.ofFloat(visibleList, "rotationY", 0f, 90f);
    visibleToInvisible.setDuration(DURATION);
    visibleToInvisible.setInterpolator(accelerator);

    final ObjectAnimator invisibleToVisible = ObjectAnimator.ofFloat(invisibleList, "rotationY",
        -90f, 0f);
    invisibleToVisible.setDuration(DURATION);
    invisibleToVisible.setInterpolator(decelerator);

    visibleToInvisible.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator anim) {
        visibleList.setVisibility(View.GONE);
        invisibleToVisible.start();
        invisibleList.setVisibility(View.VISIBLE);
      }
    });
    visibleToInvisible.start();
  }


}