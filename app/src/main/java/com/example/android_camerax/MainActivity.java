package com.example.android_camerax;

import android.Manifest;
import android.content.Context;
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
import android.os.Handler;
import android.os.HandlerThread;
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
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "MainActivity";

    private TextureView textureView;
    private CameraDevice cameraDevice;
//    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private SeekBar zoomSeekBar;
    private float maxZoom;
    private Size previewSize;

    private CameraCharacteristics cameraCharacteristics;
    private float fingerSpacing = 0;
    private float zoomLevel = 1.0f;
    private  CameraManager manager;

    private Rect zoom;

    private RulerView ruler_view ;
    float initialZoomLevel = 1.0f; // Ví dụ: mức zoom khởi tạo

    private String wideCameraId;
    private String normalCameraId;
    private CameraManager cameraManager;

    private CameraCaptureSession cameraCaptureSessions;
    private CameraViewModel cameraViewModel;

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);



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
//                updateZoom(progress);
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
                            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
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

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        ruler_view = findViewById(R.id.ruler_view);
        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        // Lấy thông số của camera và set giá trị ban đầu cho cây thước
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

                Log.d(TAG, "cameraId: " +cameraId);
                Log.d(TAG, "lensFacing: " +lensFacing);
                Log.d(TAG, "focalLengths: " + Arrays.toString(focalLengths));

                if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
//                    frontCameraId = cameraId; // Camera trước
                    Log.d(TAG, "cameraId: " +cameraId);
                }
                else if (focalLengths != null && focalLengths.length > 0) {
                    if (focalLengths[0] > 2.5) {   // Cam thường

                        normalCameraId = cameraId; // Camera thường
                        Log.d(TAG, "_onCreate _normalCameraId: " +normalCameraId);
                        maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                        ruler_view.setMaxZoomLevel(maxZoom);
//                        ruler_view.setMaxZoomLevel(8.0f);

                        ruler_view.setMinZoomLevel(1.0f);   // Khi thấy cam thường thì set là 1.0f trước

                        cameraViewModel.setValueMaxZoom(maxZoom);
                        cameraViewModel.setValueMinZoom(1.0f);

                    }  else {

                        wideCameraId = cameraId; // Camera góc rộng

//                        ruler_view.setMinZoomLevel(0.5f);  // Nếu có cam góc rộng thì set min của Zoom thành 0.5f
//                        cameraViewModel.setValueMinZoom(0.5f);

                        ruler_view.setMinZoomLevel(0.5f);  // Nếu có cam góc rộng thì set min của Zoom thành 0.5f
                        cameraViewModel.setValueMinZoom(1.0f);

                        cameraViewModel.setIsHaveUltrawideCamera(false);

                    }
                }
            }
            ruler_view.setZoomLevel(initialZoomLevel);  // Thiết lập mức zoom ban đầu
            cameraViewModel.setCurrentValueRulerView(initialZoomLevel);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        ruler_view = findViewById(R.id.ruler_view);
        // Đăng ký lắng nghe thay đổi zoom level từ RulerView
        ruler_view.setOnZoomLevelChangeListener(new RulerView.OnZoomLevelChangeListener() {
            @Override
            public void onZoomLevelChanged(float newZoomLevel) {

                Float oldCurrentValueRulerView = cameraViewModel.getCurrentValueRulerView().getValue();

                Log.d(TAG , "_setOnZoomLevelChangeListener _newZoomLevel: " +newZoomLevel);
                Log.d(TAG , "_setOnZoomLevelChangeListener _oldCurrentValueZoom: " + oldCurrentValueRulerView);

                if( oldCurrentValueRulerView < 1.0f && newZoomLevel >= 1.0f ){

                    Log.d(TAG , "_setOnZoomLevelChangeListener change to normal camera: " );
                    switchCamera(normalCameraId);
                    cameraViewModel.setCurrentValueRulerView(newZoomLevel);
                }
                else if (oldCurrentValueRulerView >= 1.0f && newZoomLevel < 1.0f ){
                    Log.d(TAG , "_setOnZoomLevelChangeListener change to ultra wide camera: " );

                    switchCamera(wideCameraId);
                    cameraViewModel.setCurrentValueRulerView(newZoomLevel);
                }else

                    setTimeout(() -> {

                        updateZoom(cameraDevice, cameraCaptureSessions, captureRequestBuilder,newZoomLevel*10);
                        Log.d(TAG, "_setOnZoomLevelChangeListener _updateZoom run newZoomLevel : " + newZoomLevel);
                    }, 100);
                cameraViewModel.setCurrentValueRulerView(newZoomLevel);

            }

        });

    }

    public static void setTimeout(Runnable runnable, int delay) {
        scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
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
            openCamera(normalCameraId); // Mặc định mở camera thường
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


    private void switchCamera(String newCameraId) {
        closeCamera();
        openCamera(newCameraId);
    }

    private void openCamera(String cameraId) {
        manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
//            String cameraId = manager.getCameraIdList()[0];
            Log.d(TAG, "_openCamera _cameraId: "+ cameraId);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);


            cameraCharacteristics = manager.getCameraCharacteristics(cameraId);

            zoomSeekBar.setMax((int) (maxZoom * 10));

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        textureView.getWidth(), textureView.getHeight());
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, null);
                cameraViewModel.setCameraDeviceId(cameraId);

//                previewSurfaceSize = getPreviewSurfaceSize();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
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
            Log.d(TAG, "_createCameraPreview previewSize.getWidth() : "+ previewSize.getWidth());
            Log.d(TAG, "_createCameraPreview previewSize.getHeight() : "+ previewSize.getHeight());

            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        return;
                    }
                    cameraCaptureSessions = session;
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
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to update camera preview", e);
        }
    }

    private void updateZoom(CameraDevice cameraDevice,CameraCaptureSession cameraCaptureSessions , CaptureRequest.Builder captureRequestBuilder, float zoomLevel) {
        if (cameraDevice == null ) {
            return;
        }
        try {
            Log.d(TAG, "_updateZoom run _cameraDevice: "+ cameraDevice.getId());
            Log.d(TAG, "_updateZoom run _cameraCaptureSessions: "+ cameraCaptureSessions);
            Log.d(TAG, "_updateZoom run _captureRequestBuilder: "+ captureRequestBuilder);
            Log.d(TAG, "_updateZoom run _zoomLevel: "+ zoomLevel);

            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Rect activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (activeRect == null) {
                Log.e(TAG, "Cannot get active array size");
                return;
            }

            float zoomFactor = (float) zoomLevel / (float) (ruler_view.getMaxZoomLevel()*10) ;
//            float zoomFactor = (float) zoomLevel / 80.0f ;

            // Ensure zoom factor is within bounds
            zoomFactor = Math.max(1f, Math.min(zoomFactor * maxZoom, maxZoom)); // 1 - 80 (sam sung s21)

            Log.d(TAG, "_updateZoom _zoomLevel:: " + zoomLevel);
            Log.d(TAG, "_updateZoom _maxZoom:: " + maxZoom);
            Log.d(TAG, "_updateZoom _zoomFactor:: " + zoomFactor);
            Log.d(TAG, "_updateZoom _ruler_view.getMaxZoomLevel():: " + ruler_view.getMaxZoomLevel()*10);

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

            Log.d(TAG, "_updateZoom zoomRect end: " + zoomRect);
            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "_updateZoom Failed to update zoom", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "_updateZoom Invalid zoom parameters", e);
        }
    }

    private void closeCamera() {
        if (cameraCaptureSessions != null) {
            cameraCaptureSessions.close();
            cameraCaptureSessions = null;
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
            openCamera(normalCameraId);
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
}