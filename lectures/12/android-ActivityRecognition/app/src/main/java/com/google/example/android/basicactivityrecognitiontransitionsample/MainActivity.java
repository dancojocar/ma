/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.example.android.basicactivityrecognitiontransitionsample;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.android.common.logger.LogFragment;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.viewbinding.BuildConfig;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Demos enabling/disabling Activity Recognition transitions, e.g., starting or stopping a walk,
 * run, drive, etc.).
 */
public class MainActivity extends AppCompatActivity {

  private final static String TAG = "MainActivity";

  private boolean activityTrackingEnabled;

  private List<ActivityTransition> activityTransitionList;

  // Action fired when transitions are triggered.
  private final String TRANSITIONS_RECEIVER_ACTION =
      BuildConfig.LIBRARY_PACKAGE_NAME + "TRANSITIONS_RECEIVER_ACTION";

  private PendingIntent mActivityTransitionsPendingIntent;
  private TransitionsReceiver mTransitionsReceiver;
  private LogFragment mLogFragment;

  private static String toActivityString(int activity) {
    switch (activity) {
      case DetectedActivity.STILL:
        return "STILL";
      case DetectedActivity.WALKING:
        return "WALKING";
      default:
        return "UNKNOWN";
    }
  }

  private static String toTransitionType(int transitionType) {
    switch (transitionType) {
      case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
        return "ENTER";
      case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
        return "EXIT";
      default:
        return "UNKNOWN";
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    mLogFragment =
        (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);

    activityTrackingEnabled = false;

    // List of activity transitions to track.
    activityTransitionList = new ArrayList<>();

    // TODO: Add activity transitions to track.
    activityTransitionList.add(new ActivityTransition.Builder()
        .setActivityType(DetectedActivity.WALKING)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        .build());
    activityTransitionList.add(new ActivityTransition.Builder()
        .setActivityType(DetectedActivity.WALKING)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        .build());
    activityTransitionList.add(new ActivityTransition.Builder()
        .setActivityType(DetectedActivity.STILL)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        .build());
    activityTransitionList.add(new ActivityTransition.Builder()
        .setActivityType(DetectedActivity.STILL)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        .build());

    // TODO: Initialize PendingIntent that will be triggered when a activity transition occurs.
    Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
    mActivityTransitionsPendingIntent =
        PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

    // TODO: Create a BroadcastReceiver to listen for activity transitions.
    // The receiver listens for the PendingIntent above that is triggered by the system when an
    // activity transition occurs.
    mTransitionsReceiver = new TransitionsReceiver();

    printToScreen("App initialized.");
  }

  @Override
  protected void onStart() {
    super.onStart();

    // TODO: Register the BroadcastReceiver to listen for activity transitions.
    registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));
  }

  @Override
  protected void onPause() {

    // TODO: Disable activity transitions when user leaves the app.
    if (activityTrackingEnabled) {
      disableActivityTransitions();
    }
    super.onPause();
  }


  @Override
  protected void onStop() {

    // TODO: Unregister activity transition receiver when user leaves the app.
    unregisterReceiver(mTransitionsReceiver);

    super.onStop();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    // Start activity recognition if the permission was approved.
    if (activityRecognitionPermissionApproved() && !activityTrackingEnabled) {
      enableActivityTransitions();
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * Registers callbacks for {@link ActivityTransition} events via a custom
   * {@link BroadcastReceiver}
   */
  private void enableActivityTransitions() {

    Log.d(TAG, "enableActivityTransitions()");


    // TODO: Create request and listen for activity changes.
    ActivityTransitionRequest request = new ActivityTransitionRequest(activityTransitionList);

    // Register for Transitions Updates.
    Task<Void> task = ActivityRecognition.getClient(this)
        .requestActivityTransitionUpdates(request, mActivityTransitionsPendingIntent);

    task.addOnSuccessListener(
        new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void result) {
            activityTrackingEnabled = true;
            printToScreen("Transitions Api was successfully registered.");

          }
        });

    task.addOnFailureListener(
        new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            printToScreen("Transitions Api could NOT be registered: " + e);
            Log.e(TAG, "Transitions Api could NOT be registered: " + e);

          }
        });
  }


  /**
   * Unregisters callbacks for {@link ActivityTransition} events via a custom
   * {@link BroadcastReceiver}
   */
  private void disableActivityTransitions() {

    Log.d(TAG, "disableActivityTransitions()");


    // TODO: Stop listening for activity changes.
    ActivityRecognition.getClient(this).removeActivityTransitionUpdates(mActivityTransitionsPendingIntent)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {
            activityTrackingEnabled = false;
            printToScreen("Transitions successfully unregistered.");
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            printToScreen("Transitions could not be unregistered: " + e);
            Log.e(TAG, "Transitions could not be unregistered: " + e);
          }
        });
  }

  /**
   * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
   * run-time permissions.
   */
  private boolean activityRecognitionPermissionApproved() {
    return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACTIVITY_RECOGNITION
    );
  }

  public void onClickEnableOrDisableActivityRecognition(View view) {

    // TODO: Enable/Disable activity tracking and ask for permissions if needed.
    if (activityRecognitionPermissionApproved()) {

      if (activityTrackingEnabled) {
        disableActivityTransitions();

      } else {
        enableActivityTransitions();
      }

    } else {
      // Request permission and start activity for result. If the permission is approved, we
      // want to make sure we start activity recognition tracking.
      Intent startIntent = new Intent(this, PermissionRationalActivity.class);
      startActivityForResult(startIntent, 0);
    }
  }

  private void printToScreen(@NonNull String message) {
    mLogFragment.getLogView().println(message);
    Log.d(TAG, message);
  }

  /**
   * Handles intents from from the Transitions API.
   */
  public class TransitionsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

      Log.d(TAG, "onReceive(): " + intent);

      if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.getAction())) {

        printToScreen("Received an unsupported action in TransitionsReceiver: action = " +
            intent.getAction());
        return;
      }

      // TODO: Extract activity transition information from listener.
      if (ActivityTransitionResult.hasResult(intent)) {

        ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);

        for (ActivityTransitionEvent event : result.getTransitionEvents()) {

          String info = "Transition: " + toActivityString(event.getActivityType()) +
              " (" + toTransitionType(event.getTransitionType()) + ")" + "   " +
              new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());

          printToScreen(info);
        }
      }
    }
  }
}
