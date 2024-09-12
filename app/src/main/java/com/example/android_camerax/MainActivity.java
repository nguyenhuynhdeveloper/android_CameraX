package com.example.android_camerax;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.graphics.ImageFormat;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private String cameraId;
    private CameraManager cameraManager;
    private String wideCameraId;
    private String normalCameraId;
    private String teleCameraId;
    private String frontCameraId;
    private  String TAG = "Wide_Angle_Camera";

    private SeekBar zoomSeekBar;
    private float maxZoomLevel;
    private float currentZoomLevel = 1f;
    private Rect activeRect;




    private SeekBar exposureSeekBar;


    // Các biến thêm mới
    private Button buttonTakePicture;
    private ImageReader imageReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        Button buttonWideCamera = findViewById(R.id.buttonWideCamera);
        Button buttonNormalCamera = findViewById(R.id.buttonNormalCamera);
        Button buttonTeleCamera = findViewById(R.id.buttonTeleCamera);
        Button buttonFrontCamera = findViewById(R.id.buttonFrontCamera);

        // Nút chụp ảnh
        buttonTakePicture = findViewById(R.id.buttonTakePicture);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        textureView.setSurfaceTextureListener(textureListener);  // đoạn gốc để mở camera

        buttonWideCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchCamera("0");
//                switchCamera(wideCameraId);
            }
        });

        buttonNormalCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchCamera("1");
//                switchCamera(normalCameraId);

            }
        });

        buttonTeleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera("2");
//                switchCamera(teleCameraId);

            }
        });

        buttonFrontCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera("3");
//                switchCamera(frontCameraId);

            }
        });

        // Xử lý khi nhấn nút chụp ảnh
        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    float[] LENS_POSE_ROTATION = characteristics.get(CameraCharacteristics.LENS_POSE_ROTATION);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    float[] LENS_DISTORTION  = characteristics.get(CameraCharacteristics.LENS_DISTORTION);
                }

                Log.d(TAG, "cameraId: " +cameraId);
                Log.d(TAG, "lensFacing: " +lensFacing);
                Log.d(TAG, "focalLengths: " + Arrays.toString(focalLengths));

                if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId; // Camera trước
                } else if (focalLengths != null && focalLengths.length > 0) {
                    if (focalLengths[0] < 2.0) {
                        wideCameraId = cameraId; // Camera góc rộng
                    } else {
                        normalCameraId = cameraId; // Camera thường
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Tính năng Phơi sáng

        exposureSeekBar = findViewById(R.id.exposureSeekBar);

// // Lấy đặc tính phơi sáng từ camera
        CameraCharacteristics characteristics = null;
        Log.d(TAG, "characteristics: " + cameraDevice);
        try {
            characteristics = cameraManager.getCameraCharacteristics("3");

        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
        Range<Integer> exposureRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);

        Log.d(TAG, "exposureRange.getUpper(): " + exposureRange.getUpper());
        Log.d(TAG, "exposureRange.getUpper(): " + exposureRange.getLower());

        exposureSeekBar.setMax(exposureRange.getUpper());
        exposureSeekBar.setMin(exposureRange.getLower());

// Lắng nghe sự thay đổi của SeekBar
        exposureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setExposureCompensation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Tính năng Zoom

        zoomSeekBar = findViewById(R.id.zoomSeekBar);

// Lấy thông tin zoom từ camera
//        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
        maxZoomLevel = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        Log.d(TAG, "maxZoomLevel: " +maxZoomLevel);

// Lắng nghe sự thay đổi của SeekBar
        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                setZoomLevel(progress);
                updateZoom(progress);
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    /// End onCreate

    private void setExposureCompensation(int exposureValue) {
        try {
            // Thiết lập giá trị bù sáng trong capture request
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureValue);

            // Cập nhật session với capture request mới
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setZoomLevel(int zoomProgress) {
        float zoomLevel = 1 + ((maxZoomLevel - 1) * zoomProgress / 100); // Tính toán mức zoom

        // Tính toán vùng cắt ảnh dựa trên mức zoom
        int cropWidth = (int) (activeRect.width() / zoomLevel);
        int cropHeight = (int) (activeRect.height() / zoomLevel);
        int cropLeft = (activeRect.width() - cropWidth) / 2;
        int cropTop = (activeRect.height() - cropHeight) / 2;
        Rect zoomRect = new Rect(cropLeft, cropTop, cropLeft + cropWidth, cropTop + cropHeight);

        // Cập nhật request cho zoom
        captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);

        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
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
            int minW = (int) (activeRect.width() / maxZoomLevel);
            int minH = (int) (activeRect.height() / maxZoomLevel);
            int difW = activeRect.width() - minW;
            int difH = activeRect.height() - minH;
            int cropW = difW * zoomLevel / 100;
            int cropH = difH * zoomLevel / 100;
            Rect zoomRect = new Rect(cropW, cropH, activeRect.width() - cropW, activeRect.height() - cropH);

            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomRect);
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            cameraId = normalCameraId;
            openCamera(normalCameraId); // Mặc định mở camera thường
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };

    private void switchCamera(String newCameraId) {
        closeCamera();
        openCamera(newCameraId);
    }

    private void openCamera(String cameraId_params) {
        try {
            cameraId = cameraId_params;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId_params, stateCallback, null);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(640, 480);
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureSeekBar.getProgress());

//            captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, activeRect);


            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {}
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (null == cameraDevice) {
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    // ---------------
    private void takePicture() {
        if (cameraDevice == null) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }

        // Thiết lập ImageReader để nhận hình ảnh
        Size[] jpegSizes = null;
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        int width = 640;
        int height = 480;
        if (jpegSizes != null && jpegSizes.length > 0) {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }

        // Thiết lập ImageReader để chụp ảnh
        imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        Surface readerSurface = imageReader.getSurface();
        imageReader.setOnImageAvailableListener(imageAvailableListener, null);

        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(readerSurface);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Lưu hình ảnh vào thư mục
            File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", "pic.jpg");

            cameraCaptureSessions.stopRepeating();
            cameraCaptureSessions.abortCaptures();

            cameraDevice.createCaptureSession(Arrays.asList(readerSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                Log.d(TAG, "Image Captured");
                                createCameraPreview();  // Quay trở lại chế độ preview sau khi chụp
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Lưu ảnh khi nó có sẵn
    private final ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                save(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }

        private void save(byte[] bytes) throws IOException {
            File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", "pic.jpg");
            try (FileOutputStream output = new FileOutputStream(file)) {
                output.write(bytes);
                Log.d(TAG, "Saved Image to: " + file.getAbsolutePath());
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            openCamera(normalCameraId); // Mặc định mở camera thường
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Quyền bị từ chối
                finish();
            }
        }
    }
}
