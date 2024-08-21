package com.example.android_camerax;


import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;

import androidx.camera.lifecycle.ProcessCameraProvider;

import androidx.camera.view.PreviewView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.widget.RelativeLayout;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private int currentOrientation = Configuration.ORIENTATION_UNDEFINED;


    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private CameraControl cameraControl;
    private CameraInfo cameraInfo;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;

    private ExecutorService cameraExecutor;

    private boolean isUsingFrontCamera = false;
    private ScaleGestureDetector scaleGestureDetector;
    private TextView countdownTextView;
    private SeekBar zoomSeekBar;



    private SensorManager sensorManager;
    private Sensor accelerometer;
    Button captureButton;
    private boolean isPortrait = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
         captureButton = findViewById(R.id.captureButton);

        // Kiểm tra nếu captureButton là null
        if (captureButton == null) {
            throw new NullPointerException("captureButton không được khởi tạo. Kiểm tra lại ID trong XML.");
        }

        // Khởi tạo SensorManager và cảm biến gia tốc
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Đăng ký SensorEventListener
        if (accelerometer != null) {
//            sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }



        Button switchCameraButton = findViewById(R.id.switchCameraButton);
//        SeekBar exposureSeekBar = findViewById(R.id.exposureSeekBar);
//        countdownTextView = findViewById(R.id.countdownTextView);
        zoomSeekBar = findViewById(R.id.zoomSeekBar);

        if (allPermissionsGranted()) {
            startCamera(); // Khi đã có đủ quyền thì mở camera lên
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);  // Hỏi quyền khi chưa có quyền
        }

        captureButton.setOnClickListener(v -> startCountdown());   // Khi click chụp ảnh thì sẽ bắt đàu đếm ngược thời gian

        switchCameraButton.setOnClickListener(v -> {
            isUsingFrontCamera = !isUsingFrontCamera;
            startCamera();
        });


        // Lắng nghe sự vuốt  slider thay đổi tỷ lệ Zoom của máy ảnh
        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (cameraControl != null && cameraInfo != null) {
                    float zoomRatio = cameraInfo.getZoomState().getValue().getMinZoomRatio()
                            + (cameraInfo.getZoomState().getValue().getMaxZoomRatio()
                            - cameraInfo.getZoomState().getValue().getMinZoomRatio()) * (progress / 100.0f);
                    cameraControl.setZoomRatio(zoomRatio);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Tạo biến quản lý việc Pinch to Zoom
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (cameraControl != null && cameraInfo != null) {
                    float currentZoomRatio = cameraInfo.getZoomState().getValue().getZoomRatio();
                    float scaleFactor = detector.getScaleFactor();
                    float newZoomRatio = currentZoomRatio * scaleFactor;

                    float minZoomRatio = cameraInfo.getZoomState().getValue().getMinZoomRatio();
                    float maxZoomRatio = cameraInfo.getZoomState().getValue().getMaxZoomRatio();
                    newZoomRatio = Math.max(minZoomRatio, Math.min(newZoomRatio, maxZoomRatio));

                    cameraControl.setZoomRatio(newZoomRatio);
                    zoomSeekBar.setProgress((int) ((newZoomRatio - minZoomRatio) / (maxZoomRatio - minZoomRatio) * 100));  // Thay đổi hiển thị của Slider tương ứng với độ Zoom đã Zoom
                }
                return true;
            }
        });

        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);   // Đưa biến _scaleGestureDetector vào sự lắng nghe của Preview
            return true;
        });

        cameraExecutor = Executors.newSingleThreadExecutor();  // Tạo ra 1 thread pool với duy nhất 1 thread thực hiện các tác vụ tuần tự


    }




    // Hàm bắt đầu khởi động camera lên
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isUsingFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                cameraControl = camera.getCameraControl();
                cameraInfo = camera.getCameraInfo();

                // Cập nhật thanh Zoom SeekBar khi khởi động camera
                zoomSeekBar.setMax(100);
                zoomSeekBar.setProgress((int) ((cameraInfo.getZoomState().getValue().getZoomRatio()
                        - cameraInfo.getZoomState().getValue().getMinZoomRatio())
                        / (cameraInfo.getZoomState().getValue().getMaxZoomRatio()
                        - cameraInfo.getZoomState().getValue().getMinZoomRatio()) * 100));

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        },
                ContextCompat.getMainExecutor(this)
        );
    }

    // Hàm bắt đầu đếm ngược : bộ đếm thời gian chụp
    private void startCountdown() {
        countdownTextView.setText("3");
        countdownTextView.setVisibility(TextView.VISIBLE);

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                countdownTextView.setVisibility(TextView.GONE);
                takePhoto();
            }

        }.start();
    }

    // Hàm chụp ảnh trên camera rồi lưu vào máy
    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }

        File photoFile = new File(getOutputDirectory(),
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                        .format(System.currentTimeMillis()) + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String msg = "Photo capture succeeded: " + photoFile.getAbsolutePath();
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);
                    }
                });
    }

    // Hàm chỉ định vị trí lưu ảnh vào trong bộ nhớ
    private File getOutputDirectory() {
        File mediaDir = getExternalMediaDirs()[0];
        if (mediaDir != null && mediaDir.exists()) {
            return mediaDir;
        } else {
            return getFilesDir();
        }
    }

    // Hàm Check quyền truy cập camera
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Hàm hỏi quyền camera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            // Xác định hướng của thiết bị
            boolean newIsPortrait = Math.abs(x) < Math.abs(y);
//            Log.d("TAG X", String.valueOf(Math.abs(x)));
//            Log.d("TAG Y", String.valueOf(Math.abs(y)));

            if (newIsPortrait != isPortrait) {
                Log.d("TAG_isPortrait", String.valueOf(isPortrait));
                isPortrait = newIsPortrait;

                Log.d("TAG_newIsPortrait", String.valueOf(newIsPortrait));
                adjustCaptureButtonLayout();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void adjustCaptureButtonLayout() {

        if (captureButton == null) {
            Log.d("TAG_captureButton", String.valueOf(captureButton));
            return;
        }
        Log.d("TAG_adjustCaptureButtonLayout", "adjustCaptureButtonLayout run ");
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) captureButton.getLayoutParams();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) captureButton.getLayoutParams();

        if (isPortrait) {
            // Đặt nút chụp ảnh vào góc dưới giữa cho chế độ portrait
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            // Đặt nút chụp ảnh vào góc dưới bên trái cho chế độ landscape
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }

        captureButton.setLayoutParams(params);
    }

    // Khi Activity bị huỷ phải tắt camera đi
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

    }
}
