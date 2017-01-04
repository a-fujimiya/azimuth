package com.example.atyk.sensorpractice;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
  private static final String TAG = "SensorPractice";
  SensorManager manager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    manager = (SensorManager) getSystemService(SENSOR_SERVICE);
  }

  @Override protected void onResume() {
    super.onResume();
    List<Sensor> sensorList = manager.getSensorList(Sensor.TYPE_ALL);
    for (Sensor sensor : sensorList) {
      final int sensorType = sensor.getType();
      Log.v(TAG, sensor.getName() + ", " + sensorType);
      if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        continue;
      }
      if (sensorType == Sensor.TYPE_ACCELEROMETER) {
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
      }
    }
  }

  @Override protected void onPause() {
    manager.unregisterListener(this);
    super.onPause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    manager = null;
  }

  private void updateDirectionView(String orientation) {
    final TextView orientationView = (TextView) findViewById(R.id.orientation_text);
    orientationView.setText(orientation);
  }

  private static final int MATRIX_SIZE = 16;
  private float[] inR = new float[MATRIX_SIZE];
  private float[] outR = new float[MATRIX_SIZE];
  private float[] I = new float[MATRIX_SIZE];
  private float[] orientationValues = new float[3];
  private float[] magneticValues;
  private float[] accelerometerValues;

  @Override public void onSensorChanged(SensorEvent sensorEvent) {
    switch (sensorEvent.sensor.getType()) {
      case Sensor.TYPE_MAGNETIC_FIELD:
        magneticValues = sensorEvent.values.clone();
        break;
      case Sensor.TYPE_ACCELEROMETER:
        accelerometerValues = sensorEvent.values.clone();
        break;
      default:
        Log.d(TAG, "onSensorChanged: " + sensorEvent.sensor.getName());
    }
    if (magneticValues != null && accelerometerValues != null) {
      if (SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticValues)) {
        if (SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR)) {
          SensorManager.getOrientation(outR, orientationValues);
          final int azimuth = radian2Degrees(orientationValues[0]);
          final int pitch = radian2Degrees(orientationValues[1]);
          final int roll = radian2Degrees(orientationValues[2]);
          Log.i(TAG, String.format("azimuth: %d, pitch: %d, roll: %d", azimuth, pitch, roll));
          updateDirectionView(getOrientation(orientationValues[0]));
        }
      }
    }
  }

  @Override public void onAccuracyChanged(Sensor sensor, int i) {
    Log.i(TAG, "onAccuracyChanged: " + sensor.getName() + ", accuracy: " + i);
  }

  private int radian2Degrees(float radian) {
    if (Float.compare(radian, 0f) >= 0) {
      return (int) Math.floor(Math.toDegrees(radian));
    } else {
      return (int) Math.floor(Math.toDegrees(radian)) + 360;
    }
  }

  private String getOrientation(float radian) {
    final double[] orientation_range = {
        -(Math.PI * 7 / 8.0), // S
        -(Math.PI * 5 / 8.0), // SW
        -(Math.PI * 3 / 8.0), // W
        -(Math.PI * 1 / 8.0), // NW
        +(Math.PI * 1 / 8.0), // N
        +(Math.PI * 3 / 8.0), // NE
        +(Math.PI * 5 / 8.0), // E
        +(Math.PI * 7 / 8.0), // SE
    };
    for (int i = 0; i < orientation_range.length; i++) {
      if (Double.compare(radian,orientation_range[i]) < 0) {
        return (getResources().getStringArray(R.array.orientation_names))[i];
      }
    }
    return getString(R.string.south);
  }
}
