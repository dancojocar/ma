package com.example.ma.sm.sensors;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import com.example.ma.sm.fragments.BaseActivity;

public class AccelerationDemo extends BaseActivity {
  private SensorManager mSensorManager;
  private GraphView mGraphView;

  private class GraphView extends View implements SensorEventListener {
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Canvas mCanvas = new Canvas();
    private Path mPath = new Path();
    private RectF mRect = new RectF();
    private float mLastValues[] = new float[3];
    private int mColors[] = new int[3];
    private float mLastX;
    private float mScale;
    private float mYOffset;
    private float mMaxX;
    private float mSpeed = 1.0f;
    private float mWidth;
    private float mHeight;

    public GraphView(Context context) {
      super(context);
      mColors[0] = Color.BLACK; // magnetic field
      mColors[1] = Color.RED; // accelerometer
      mColors[2] = Color.GREEN; // orientation

      mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
      mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
      mPath.arcTo(mRect, 0, 180);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
      mCanvas.setBitmap(mBitmap);
      mCanvas.drawColor(0xFFFFFFFF);
      mYOffset = h * 0.5f;
      mScale = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
      mWidth = w;
      mHeight = h;
      if (mWidth < mHeight) { //portrait mode
        mMaxX = w;
      } else { //landscape
        mMaxX = w - 50;
      }
      mLastX = mMaxX;
      super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      synchronized (this) {
        if (mBitmap != null) {
          final Paint paint = mPaint;
          if (mLastX >= mMaxX) {
            // prepare the screen
            mLastX = 0;
            final Canvas cavas = mCanvas;
            final float yoffset = mYOffset;
            final float maxx = mMaxX;
            final float oneG = SensorManager.STANDARD_GRAVITY * mScale;
            paint.setColor(0xFFAAAAAA);
            cavas.drawColor(0xFFFFFFFF);
            cavas.drawLine(0, yoffset, maxx, yoffset, paint);
            cavas.drawLine(0, yoffset + oneG, maxx, yoffset + oneG, paint);
            cavas.drawLine(0, yoffset - oneG, maxx, yoffset - oneG, paint);
          }
          canvas.drawBitmap(mBitmap, 0, 0, null);
        }
      }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//      Timber.v("sensor: %s x: %f y: %f z: %f", event.sensor.getName(), event.values[0], event.values[1], event.values[2]);
      synchronized (this) {
        if (mBitmap != null) {
          final float oneG = SensorManager.STANDARD_GRAVITY * mScale;

          final Canvas canvas = mCanvas;
          final Paint paint = mPaint;
          float deltaX = mSpeed;
          float newX = mLastX + deltaX;

          float v = mYOffset + Math.max(event.values[0], Math.max(event.values[1], event.values[2]));
          int i = 0; // magnetic field
          if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            i = 1; // accelerometer
            v += oneG;
          } else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            i = 2; // orientation
            v -= oneG;
//          } else {
//            Timber.v("magnetic field: %s x: %f y: %f z: %f", event.sensor.getName(), event.values[0], event.values[1], event.values[2]);
          }
          paint.setColor(mColors[i]);
          paint.setStrokeWidth(15);
          canvas.drawLine(mLastX, mLastValues[i], newX, v, paint);
          mLastValues[i] = v;
          if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            //advance the points
            mLastX += mSpeed;
          }
          invalidate();
        }
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // Be sure to call the super class.
    super.onCreate(savedInstanceState);

    mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    mGraphView = new GraphView(this);
    setContentView(mGraphView);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSensorManager.registerListener(mGraphView,
        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_FASTEST);
    mSensorManager.registerListener(mGraphView,
        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        SensorManager.SENSOR_DELAY_FASTEST);
    mSensorManager.registerListener(mGraphView,
        mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        SensorManager.SENSOR_DELAY_FASTEST);
  }

  @Override
  protected void onStop() {
    mSensorManager.unregisterListener(mGraphView);
    super.onStop();
  }
}