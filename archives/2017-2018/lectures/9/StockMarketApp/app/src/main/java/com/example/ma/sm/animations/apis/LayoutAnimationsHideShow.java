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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.example.ma.sm.R;

/**
 * This application demonstrates how to use LayoutTransition to
 * automate transition animations as items are hidden or shown
 * in a container.
 */
public class LayoutAnimationsHideShow extends Activity {

  ViewGroup container = null;
  private LayoutTransition mTransitioner;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_animations_hideshow);

    final CheckBox hideGoneCB = findViewById(R.id.hideGoneCB);

    container = new LinearLayout(this);
    container.setLayoutParams(
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));

    // Add a slew of buttons to the container.
    // We won't add any more buttons at runtime, but
    // will just show/hide the buttons we've already created
    int numButtons = 4;
    for (int i = 0; i < numButtons; ++i) {
      Button newButton = new Button(this);
      newButton.setText(String.valueOf(i));
      container.addView(newButton);
      newButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          v.setVisibility(hideGoneCB.isChecked() ?
              View.GONE :
              View.INVISIBLE);
        }
      });
    }

    resetTransition();

    ViewGroup parent = findViewById(R.id.parent);
    parent.addView(container);

    Button addButton = findViewById(R.id.addNewButton);
    addButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        for (int i = 0; i < container.getChildCount(); ++i) {
          View view = container.getChildAt(i);
          view.setVisibility(View.VISIBLE);
        }
      }
    });

    CheckBox customAnimCB = findViewById(R.id.customAnimCB);
    customAnimCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        long duration;
        if (isChecked) {
          mTransitioner.setStagger(LayoutTransition.CHANGE_APPEARING,
              300);
          mTransitioner.setStagger(LayoutTransition.CHANGE_DISAPPEARING,
              300);
          setupCustomAnimations();
          duration = 500;
        } else {
          resetTransition();
          duration = 300;
        }
        mTransitioner.setDuration(duration);
      }
    });
  }

  private void resetTransition() {
    mTransitioner = new LayoutTransition();
    container.setLayoutTransition(mTransitioner);
  }

  private void setupCustomAnimations() {
    // Adding
    ObjectAnimator animIn = ObjectAnimator.ofFloat(null,
        "rotationY", 90f, 0f).
        setDuration(mTransitioner.getDuration(LayoutTransition.APPEARING));
    mTransitioner.setAnimator(LayoutTransition.APPEARING, animIn);
    animIn.addListener(new AnimatorListenerAdapter() {
      public void onAnimationEnd(Animator anim) {
        View view = (View) ((ObjectAnimator) anim).getTarget();
        if (view != null) {
          view.setRotationY(0f);
        }
      }
    });

    // Removing
    ObjectAnimator animOut = ObjectAnimator.ofFloat(null,
        "rotationX", 0f, 90f).
        setDuration(mTransitioner.getDuration(LayoutTransition.DISAPPEARING));
    mTransitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
    animOut.addListener(new AnimatorListenerAdapter() {
      public void onAnimationEnd(Animator anim) {
        View view = (View) ((ObjectAnimator) anim).getTarget();
        if (view != null) {
          view.setRotationX(0f);
        }
      }
    });
  }
}