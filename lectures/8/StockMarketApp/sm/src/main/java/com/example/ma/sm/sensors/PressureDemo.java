package com.example.ma.sm.sensors;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.ma.sm.R;
import com.example.ma.sm.fragments.BaseActivity;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PressureDemo extends BaseActivity implements SensorEventListener {

  @BindView(R.id.chart)
  FrameLayout view;
  @BindView(R.id.current_pressure)
  TextView pressureText;

  private SensorManager sensorManager;
  private Sensor pressure;
  private XYSeries series;
  private XYMultipleSeriesDataset dataset;
  private GraphicalView chartView;
  private long counter;
  private long now;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pressure);
    ButterKnife.bind(this);

    // Get an instance of the sensor service, and use that to get an instance of
    // a particular sensor.
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
    for (Sensor s : sensors) {
      Timber.v("Sensor: %s type: %d ", s.getName(), s.getType());
    }
    pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    if (pressure != null) {

      series = new XYSeries("Pressure");
      Random r = new Random();
      //add some bogus data
      for (counter = 0; counter < 10; counter++) {
        series.add(counter, 970 + 10 * r.nextDouble());
      }

      // Create the renderer
      XYSeriesRenderer renderer = new XYSeriesRenderer();
      renderer.setLineWidth(5);
      renderer.setColor(Color.RED);
      renderer.setDisplayBoundingPoints(true);
      renderer.setPointStyle(PointStyle.CIRCLE);
      renderer.setPointStrokeWidth(10);

      // register the renderer
      XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
      multiRenderer.addSeriesRenderer(renderer);
      multiRenderer.setLabelsTextSize(30);


      // We want to avoid black border
      multiRenderer.setMarginsColor(Color.argb(0, 128, 0, 0)); // transparent margins
      // Disable Pan on two axis
      multiRenderer.setPanEnabled(false, false);
      multiRenderer.setYAxisMax(1100);
      multiRenderer.setYAxisMin(0);
      multiRenderer.setShowGrid(true); // we show the grid

      dataset = new XYMultipleSeriesDataset();
      dataset.addSeries(series);

      chartView = ChartFactory.getLineChartView(this, dataset, multiRenderer);

      view.addView(chartView);
    } else {
      TextView tv = new TextView(this);
      tv.setText(R.string.pressure_sensor_not_available);
      view.addView(tv);
    }
    now = System.currentTimeMillis();
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    long time = System.currentTimeMillis();
    if (time - now > 1 * 1000) {
      now = time;
      float currentPressure = sensorEvent.values[0];
      Timber.v("Current pressure: %f", currentPressure);
      pressureText.setText(String.valueOf(currentPressure));
      dataset.removeSeries(series);
      series.add(counter++, currentPressure);
      dataset.addSeries(series);
      chartView.repaint();
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {
  }

  @Override
  protected void onResume() {
    // Register a listener for the sensor.
    super.onResume();
    sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected void onPause() {
    // Be sure to unregister the sensor when the activity pauses.
    super.onPause();
    sensorManager.unregisterListener(this);
  }
}
