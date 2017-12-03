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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ma.sm.R;

/**
 * This demo shows how the AnimatorListener events work.
 */
public class AnimatorEvents extends Activity {

  TextView startText,
      repeatText,
      cancelText,
      endText;
  TextView startTextAnimator,
      repeatTextAnimator,
      cancelTextAnimator,
      endTextAnimator;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.animator_events);
    LinearLayout container = findViewById(R.id.container);
    final MyAnimationView animView = new MyAnimationView(this);
    container.addView(animView);
    startText = findViewById(R.id.startText);
    startText.setAlpha(.5f);
    repeatText = findViewById(R.id.repeatText);
    repeatText.setAlpha(.5f);
    cancelText = findViewById(R.id.cancelText);
    cancelText.setAlpha(.5f);
    endText = findViewById(R.id.endText);
    endText.setAlpha(.5f);
    startTextAnimator = findViewById(R.id.startTextAnimator);
    startTextAnimator.setAlpha(.5f);
    repeatTextAnimator = findViewById(R.id.repeatTextAnimator);
    repeatTextAnimator.setAlpha(.5f);
    cancelTextAnimator = findViewById(R.id.cancelTextAnimator);
    cancelTextAnimator.setAlpha(.5f);
    endTextAnimator = findViewById(R.id.endTextAnimator);
    endTextAnimator.setAlpha(.5f);
    final CheckBox endCB = findViewById(R.id.endCB);


    Button starter = findViewById(R.id.startButton);
    starter.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        animView.startAnimation(endCB.isChecked());
      }
    });

    Button canceler = findViewById(R.id.cancelButton);
    canceler.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        animView.cancelAnimation();
      }
    });

    Button ender = findViewById(R.id.endButton);
    ender.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        animView.endAnimation();
      }
    });

  }

  public class MyAnimationView extends View implements Animator.AnimatorListener,
      ValueAnimator.AnimatorUpdateListener {

    Animator animation;
    ShapeHolder ball = null;
    boolean endImmediately = false;

    public MyAnimationView(Context context) {
      super(context);
      ball = createBall();
    }

    private void createAnimation() {
      if (animation == null) {
        ObjectAnimator yAnim = ObjectAnimator.ofFloat(ball, "y",
            ball.getY(), getHeight() - 50f).setDuration(1500);
        yAnim.setRepeatCount(2);
        yAnim.setRepeatMode(ValueAnimator.REVERSE);
        yAnim.setInterpolator(new AccelerateInterpolator(2f));
        yAnim.addUpdateListener(this);
        yAnim.addListener(this);

        ObjectAnimator xAnim = ObjectAnimator.ofFloat(ball, "x",
            ball.getX(), ball.getX() + getWidth() / 2).setDuration(1000);
        xAnim.setStartDelay(0);
        xAnim.setRepeatCount(2);
        xAnim.setRepeatMode(ValueAnimator.REVERSE);
        xAnim.setInterpolator(new AccelerateInterpolator(2f));

        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(ball, "alpha", 1f, 0f, 1f).
            setDuration(1500);
        alphaAnim.start();

        animation = new AnimatorSet();
        ((AnimatorSet) animation).playTogether(yAnim, xAnim);
        animation.addListener(this);
      }
    }

    public void startAnimation(boolean endImmediately) {
      this.endImmediately = endImmediately;
      startText.setAlpha(.5f);
      repeatText.setAlpha(.5f);
      cancelText.setAlpha(.5f);
      endText.setAlpha(.5f);
      startTextAnimator.setAlpha(.5f);
      repeatTextAnimator.setAlpha(.5f);
      cancelTextAnimator.setAlpha(.5f);
      endTextAnimator.setAlpha(.5f);
      int gray = ContextCompat.getColor(getContext(), android.R.color.darker_gray);
      startText.setTextColor(gray);
      repeatText.setTextColor(gray);
      cancelText.setTextColor(gray);
      endText.setTextColor(gray);
      startTextAnimator.setTextColor(gray);
      repeatTextAnimator.setTextColor(gray);
      cancelTextAnimator.setTextColor(gray);
      endTextAnimator.setTextColor(gray);
      createAnimation();
      animation.start();
    }

    public void cancelAnimation() {
      createAnimation();
      animation.cancel();
    }

    public void endAnimation() {
      createAnimation();
      animation.end();
    }


    private ShapeHolder createBall() {
      OvalShape circle = new OvalShape();
      circle.resize(50f, 50f);
      ShapeDrawable drawable = new ShapeDrawable(circle);
      ShapeHolder shapeHolder = new ShapeHolder(drawable);
      shapeHolder.setX(0f);
      shapeHolder.setY(0f);
      int red = (int) (Math.random() * 255);
      int green = (int) (Math.random() * 255);
      int blue = (int) (Math.random() * 255);
      int color = 0xff000000 | red << 16 | green << 8 | blue;
      Paint paint = drawable.getPaint(); //new Paint(Paint.ANTI_ALIAS_FLAG);
      int darkColor = 0xff000000 | red / 4 << 16 | green / 4 << 8 | blue / 4;
      RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
          50f, color, darkColor, Shader.TileMode.CLAMP);
      paint.setShader(gradient);
      shapeHolder.setPaint(paint);
      return shapeHolder;
    }

    @Override
    protected void onDraw(Canvas canvas) {
      canvas.save();
      canvas.translate(ball.getX(), ball.getY());
      ball.getShape().draw(canvas);
      canvas.restore();
    }

    public void onAnimationUpdate(ValueAnimator animation) {
      invalidate();
    }

    public void onAnimationStart(Animator animation) {
      int color = ContextCompat.
          getColor(getContext(), android.R.color.holo_red_dark);
      if (animation instanceof AnimatorSet) {
        startText.setAlpha(1f);
        startText.setTextColor(color);
      } else {
        startTextAnimator.setAlpha(1f);
        startTextAnimator.setTextColor(color);
      }
      if (endImmediately) {
        animation.end();
      }
    }

    public void onAnimationEnd(Animator animation) {
      int color = ContextCompat.
          getColor(getContext(), android.R.color.holo_red_dark);
      if (animation instanceof AnimatorSet) {
        endText.setAlpha(1f);
        endText.setTextColor(color);
      } else {
        endTextAnimator.setAlpha(1f);
        endTextAnimator.setTextColor(color);
      }
    }

    public void onAnimationCancel(Animator animation) {
      int color = ContextCompat.
          getColor(getContext(), android.R.color.holo_red_dark);
      if (animation instanceof AnimatorSet) {
        cancelText.setAlpha(1f);
        cancelText.setTextColor(color);
      } else {
        cancelTextAnimator.setAlpha(1f);
        cancelTextAnimator.setTextColor(color);
      }
    }

    public void onAnimationRepeat(Animator animation) {
      int color = ContextCompat.
          getColor(getContext(), android.R.color.holo_red_dark);
      if (animation instanceof AnimatorSet) {
        repeatText.setAlpha(1f);
        repeatText.setTextColor(color);
      } else {
        repeatTextAnimator.setAlpha(1f);
        repeatTextAnimator.setTextColor(color);
      }
    }
  }
}