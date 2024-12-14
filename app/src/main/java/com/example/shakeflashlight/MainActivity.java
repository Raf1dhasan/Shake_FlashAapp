package com.example.shakeflashlight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private CameraManager cameraManager;
    private String cameraId;
    private TextView flashlightStatus;
    private boolean isFlashlightOn = false;

    // Thresholds for shake detection
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_WAIT_TIME_MS = 500;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashlightStatus = findViewById(R.id.flashlightStatus);

        // Initialize SensorManager and accelerometer sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Initialize CameraManager for controlling the flashlight
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    // Detect shake and toggle flashlight
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate acceleration magnitude
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
            long currentTime = System.currentTimeMillis();

            // Check if shake exceeds threshold and is within the wait time
            if (acceleration > SHAKE_THRESHOLD && (currentTime - lastShakeTime > SHAKE_WAIT_TIME_MS)) {
                lastShakeTime = currentTime;
                toggleFlashlight();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this app but required for interface
    }

    // Toggle flashlight on/off using CameraManager
    private void toggleFlashlight() {
        try {
            if (isFlashlightOn) {
                // Turn off the flashlight
                cameraManager.setTorchMode(cameraId, false);
                isFlashlightOn = false;
                flashlightStatus.setText("Flashlight OFF");
            } else {
                // Turn on the flashlight
                cameraManager.setTorchMode(cameraId, true);
                isFlashlightOn = true;
                flashlightStatus.setText("Flashlight ON");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Handle camera permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with camera access
        }
    }

    // Register sensor listener when app is active
    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // Unregister sensor listener when app is paused
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
