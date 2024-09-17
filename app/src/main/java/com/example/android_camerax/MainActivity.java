package com.example.android_camerax;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "MainActivity";

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private SeekBar zoomSeekBar;
    private float maxZoom;
    private Size previewSize;

    private CameraCharacteristics cameraCharacteristics;
    private float fingerSpacing = 0;
    private float zoomLevel = 1.0f;
    private  CameraManager manager;

    private Rect zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        zoomSeekBar = findViewById(R.id.zoomSeekBar);

        if (checkCameraPermission()) {
            textureView.setSurfaceTextureListener(textureListener);
        } else {
            requestCameraPermission();
        }

        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateZoom(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        // Thiết lập OnTouchListener cho TextureView
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (cameraCharacteristics == null) return false;
                Rect rect = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                if (rect == null) return false;

                float currentFingerSpacing;
                if (event.getPointerCount() == 2) { // Multi touch.
                    currentFingerSpacing = getFingerSpacing(event);
                    float delta = 10.0f; // Điều chỉnh giá trị này để điều chỉnh độ nhạy của zoom
                    if (fingerSpacing != 0) {
                        Log.d(TAG, "_fingerSpacing :" + fingerSpacing);
                        if (currentFingerSpacing > fingerSpacing && zoomLevel < maxZoom) {
                            zoomLevel += delta;
                            Log.d(TAG, "_zoomLevel ++ :" + zoomLevel);
                        } else if (currentFingerSpacing < fingerSpacing && zoomLevel > 1) {
                            zoomLevel -= delta;
                            Log.d(TAG, "_zoomLevel -- :" + zoomLevel);
                        }
                        int minW = (int) (rect.width() / maxZoom);
                        int minH = (int) (rect.height() / maxZoom);
                        int difW = rect.width() - minW;
                        int difH = rect.height() - minH;
                        int cropW = difW / 100 * (int) zoomLevel;
                        int cropH = difH / 100 * (int) zoomLevel;
                        cropW -= cropW & 3;
                        cropH -= cropH & 3;
                        zoom = new Rect(cropW, cropH, rect.width() - cropW, rect.height() - cropH);
                        captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                        try {
                            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    fingerSpacing = currentFingerSpacing;
                    Log.d(TAG, "_fingerSpacing after:" + fingerSpacing);

                } else {
                    fingerSpacing = 0;
                }
                return true;
            }
        });


    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                textureView.setSurfaceTextureListener(textureListener);
            } else {
                Toast.makeText(this, "Camera permission is required to use this app", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Adjust preview here if needed
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private void openCamera() {
         manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            cameraCharacteristics = manager.getCameraCharacteristics(cameraId);

            zoomSeekBar.setMax((int) (maxZoom * 10));

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        textureView.getWidth(), textureView.getHeight());
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to open camera", e);
            Toast.makeText(this, "Failed to open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        Size optimalSize = choices[0];
        for (Size size : choices) {
            if (size.getWidth() <= width && size.getHeight() <= height &&
                    size.getWidth() > optimalSize.getWidth() && size.getHeight() > optimalSize.getHeight()) {
                optimalSize = size;
            }
        }
        return optimalSize;
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            closeCamera();
            Log.e(TAG, "Camera device error: " + error);
            Toast.makeText(MainActivity.this, "Camera error: " + error, Toast.LENGTH_SHORT).show();
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Failed to configure camera capture session");
                    Toast.makeText(MainActivity.this, "Failed to configure camera", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to create camera preview", e);
            Toast.makeText(this, "Failed to create camera preview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to update camera preview", e);
        }
    }

    private void updateZoom(int zoomLevel) {
        if (cameraDevice == null) {
            return;
        }
        try {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Rect activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (activeRect == null) {
                Log.e(TAG, "Cannot get active array size");
                return;
            }

            float zoomFactor = (float) zoomLevel / zoomSeekBar.getMax();

            // Ensure zoom factor is within bounds
            zoomFactor = Math.max(1f, Math.min(zoomFactor * maxZoom, maxZoom)); // 1 - 80 (sam sung s21)

            Log.e(TAG, "zoomLevel:: " + zoomLevel);
            Log.e(TAG, "maxZoom:: " + maxZoom);
            Log.e(TAG, "zoomFactor:: " + zoomFactor);

            // Tính toán lại vùng crop
            int minW = (int) (activeRect.width() / maxZoom);
            int minH = (int) (activeRect.height() / maxZoom);

            int difW = activeRect.width() - minW;
            int difH = activeRect.height() - minH;

            int cropW = (int) (difW * (zoomFactor - 1) / (maxZoom - 1) / 2);
            int cropH = (int) (difH * (zoomFactor - 1) / (maxZoom - 1) / 2);

            // Ensure crop values are within bounds
            cropW = Math.max(0, Math.min(cropW, activeRect.width() / 2));
            cropH = Math.max(0, Math.min(cropH, activeRect.height() / 2));

            Rect zoomRect = new Rect(cropW, cropH, activeRect.width() - cropW, activeRect.height() - cropH);

            Log.d(TAG, "zoomRect end: " + zoomRect);
            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to update zoom", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid zoom parameters", e);
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }


    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
}