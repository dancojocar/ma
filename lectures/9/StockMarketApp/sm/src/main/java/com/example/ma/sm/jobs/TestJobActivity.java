package com.example.ma.sm.jobs;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ma.sm.R;
import com.example.ma.sm.fragments.BaseActivity;
import com.example.ma.sm.service.TestJobService;

import java.lang.ref.WeakReference;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class TestJobActivity extends BaseActivity {

  public static final int MSG_UNCOLOUR_START = 0;
  public static final int MSG_UNCOLOUR_STOP = 1;
  public static final int MSG_SERVICE_OBJ = 2;
  private static int kJobId = 0;
  // UI fields.
  @BindColor(R.color.none_received)
  int defaultColor;
  @BindColor(R.color.start_received)
  int startJobColor;
  @BindColor(R.color.stop_received)
  int stopJobColor;
  @BindView(R.id.onstart_textview)
  TextView mShowStartView;
  @BindView(R.id.onstop_textview)
  TextView mShowStopView;
  @BindView(R.id.task_params)
  TextView mParamsTextView;
  @BindView(R.id.delay_time)
  EditText mDelayEditText;
  @BindView(R.id.deadline_time)
  EditText mDeadlineEditText;
  @BindView(R.id.checkbox_unmetered)
  RadioButton mWiFiConnectivityRadioButton;
  @BindView(R.id.checkbox_any)
  RadioButton mAnyConnectivityRadioButton;
  @BindView(R.id.checkbox_charging)
  CheckBox mRequiresChargingCheckBox;
  @BindView(R.id.checkbox_idle)
  CheckBox mRequiresIdleCheckbox;
  ComponentName mServiceComponent;
  /**
   * Service object to interact scheduled jobs.
   */
  TestJobService mTestService;
  private MyHandler handler = new MyHandler(this);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_job);
    ButterKnife.bind(this);

    mServiceComponent = new ComponentName(this, TestJobService.class);
    // Start service and provide it a way to communicate with us.
    Intent startServiceIntent = new Intent(this, TestJobService.class);
    startServiceIntent.putExtra("messenger", new Messenger(handler));
    startService(startServiceIntent);
  }

  private boolean ensureTestService() {
    if (mTestService == null) {
      Toast.makeText(TestJobActivity.this, "Service null, never got callback?",
          Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  @OnClick(R.id.schedule_button)
  public void scheduleJob(View v) {
    if (!ensureTestService()) {
      return;
    }

    JobInfo.Builder builder = new JobInfo.Builder(kJobId++, mServiceComponent);

    String delay = mDelayEditText.getText().toString();
    if (!TextUtils.isEmpty(delay)) {
      builder.setMinimumLatency(Long.valueOf(delay) * 1000);
    }
    String deadline = mDeadlineEditText.getText().toString();
    if (!TextUtils.isEmpty(deadline)) {
      builder.setOverrideDeadline(Long.valueOf(deadline) * 1000);
    }
    boolean requiresUnmetered = mWiFiConnectivityRadioButton.isChecked();
    boolean requiresAnyConnectivity = mAnyConnectivityRadioButton.isChecked();
    if (requiresUnmetered) {
      builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
    } else if (requiresAnyConnectivity) {
      builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
    }
    builder.setRequiresDeviceIdle(mRequiresIdleCheckbox.isChecked());
    builder.setRequiresCharging(mRequiresChargingCheckBox.isChecked());

    mTestService.scheduleJob(builder.build());
  }

  @OnClick(R.id.cancel_button)
  public void cancelAllJobs(View v) {
    JobScheduler tm =
        (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    tm.cancelAll();
  }

  /**
   * UI onclick listener to call jobFinished() in our service.
   */
  @OnClick(R.id.finished_button)
  public void finishJob(View v) {
    if (!ensureTestService()) {
      return;
    }
    mTestService.callJobFinished();
    mParamsTextView.setText("");
  }

  /**
   * Receives callback from the service when a job has landed
   * on the app. Colours the UI and post a message to
   * uncolour it after a second.
   */
  public void onReceivedStartJob(JobParameters params) {
    mShowStartView.setBackgroundColor(startJobColor);
    Message m = Message.obtain(handler, MSG_UNCOLOUR_START);
    handler.sendMessageDelayed(m, 1000L); // uncolour in 1 second.
    mParamsTextView.setText("Executing: " + params.getJobId() + " " + params.getExtras());
  }

  /**
   * Receives callback from the service when a job that
   * previously landed on the app must stop executing.
   * Colours the UI and post a message to uncolour it after a
   * second.
   */
  public void onReceivedStopJob() {
    mShowStopView.setBackgroundColor(stopJobColor);
    Message m = Message.obtain(handler, MSG_UNCOLOUR_STOP);
    handler.sendMessageDelayed(m, 2000L); // uncolour in 1 second.
    mParamsTextView.setText("");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mTestService.setUiCallback(null);
  }

  private static class MyHandler extends Handler {
    private final WeakReference<TestJobActivity> wActivity;


    public MyHandler(TestJobActivity activity) {
      this.wActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
      TestJobActivity activity = wActivity.get();
      if (activity != null) {
        switch (msg.what) {
          case MSG_UNCOLOUR_START:
            activity.mShowStartView.setBackgroundColor(activity.defaultColor);
            break;
          case MSG_UNCOLOUR_STOP:
            activity.mShowStopView.setBackgroundColor(activity.defaultColor);
            break;
          case MSG_SERVICE_OBJ:
            activity.mTestService = (TestJobService) msg.obj;
            activity.mTestService.setUiCallback(activity);
        }
      }
    }
  }
}