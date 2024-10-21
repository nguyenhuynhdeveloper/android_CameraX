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
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Arrays;
import java.util.Set;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        Button buttonWideCamera = findViewById(R.id.buttonWideCamera);
        Button buttonNormalCamera = findViewById(R.id.buttonNormalCamera);
        Button buttonTeleCamera = findViewById(R.id.buttonTeleCamera);
        Button buttonFrontCamera = findViewById(R.id.buttonFrontCamera);
        Button getCharateristics = findViewById(R.id.getCharacteristics);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        textureView.setSurfaceTextureListener(textureListener);

        buttonWideCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchCamera("12");
//                switchCamera(wideCameraId);
            }
        });

        buttonNormalCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera("13");
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

        getCharateristics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectWideAngleCamera(MainActivity.this);
            }
        });


        detectWideAngleCamera(MainActivity.this);
//        try {
//
//            // // Lấy ra các camera có trên thiết bị
//            for (String cameraId : cameraManager.getCameraIdList()) {
//                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
//                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
//                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
//
//                Log.d(TAG, "cameraId: " +cameraId);
//                Log.d(TAG, "lensFacing: " +lensFacing);
//                Log.d(TAG, "focalLengths: " + Arrays.toString(focalLengths));
//
//                if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
//                    frontCameraId = cameraId; // Camera trước
//                } else if (focalLengths != null && focalLengths.length > 0) {
//                    if (focalLengths[0] < 2.0) {
//                        wideCameraId = cameraId; // Camera góc rộng
//                    } else if (focalLengths[0] > 4.0) {
//                        teleCameraId = cameraId; // Camera tele
//                    } else {
//                        normalCameraId = cameraId; // Camera thường
//                    }
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
    }

//    public void detectWideAngleCamera(Context context) {
//        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//
//        try {
//            // Get the list of available camera IDs
//            String[] cameraIds = manager.getCameraIdList();
//            Log.d(TAG, "_cameraIds: "+ Arrays.toString(cameraIds));
//
//            for (String cameraId : cameraIds) {
//                Log.d(TAG, "_cameraIds cameraId: "+ cameraId);
//                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//
//                Log.d(TAG, "_cameraIds characteristics: "+ characteristics.toString());
//
//
//                // Get focal lengths (usually in millimeters)
//                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
//                Log.d(TAG, "_cameraIds focalLengths: "+ Arrays.toString(focalLengths));
//
//                // Get sensor size
//                SizeF sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
//                Log.d(TAG, "_cameraIds sensorSize: "+ sensorSize);
//
//                if (focalLengths != null && sensorSize != null) {
//                    for (float focalLength : focalLengths) {
//                        // Calculate field of view or check based on known values
//                        // For wide or ultra-wide, focalLength will be typically less than ~4-5mm (depends on sensor)
//                        if (focalLength < 5.0f) {
//                            // This could be a wide or ultra-wide camera
//                            System.out.println("Camera ID " + cameraId + " is wide/ultra-wide");
//                        }
//                    }
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }


//    public void detectWideAngleCamera(Context context) {
//        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//
//        try {
//            // Get the list of available camera IDs
//            String[] cameraIds = manager.getCameraIdList();
//            Log.d(TAG, "_cameraIds: "+ Arrays.toString(cameraIds));
//
//            for (String cameraId : cameraIds) {
//                Log.d(TAG, "_cameraIds cameraId: "+ cameraId);
//                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//
//                Log.d(TAG, "_cameraIds characteristics: "+ characteristics.toString());
//
//
//                // Get the camera lens focal lengths (in millimeters)
//                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
//                Log.d(TAG, "_cameraIds focalLengths: "+ Arrays.toString(focalLengths));
//
//                // Get sensor size
//                SizeF sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
//                Log.d(TAG, "_cameraIds sensorSize: "+ sensorSize);
//
//                if (focalLengths != null && sensorSize != null) {
//                    for (float focalLength : focalLengths) {
//                        // Check focal length to identify wide-angle or ultra-wide-angle cameras
//                        if (focalLength < 5.0f) {
//                            if (focalLength < 2.0f) {
//                                // Typically ultra-wide-angle lens in Pixel phones
//                                System.out.println("_cameraIds: Camera ID " + cameraId + " is an Ultra-Wide camera.");
//                            } else {
//                                // Wide-angle lens
//                                System.out.println("_cameraIds: Camera ID " + cameraId + " is a Wide camera.");
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

    public void detectWideAngleCamera(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String cameraId : cameraIds) {
                Log.d(TAG, "_cameraIds cameraId: "+ cameraId);
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                // Get the sensor array size
                Rect sensorRect = characteristics.get(characteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                Log.d(TAG, "_cameraIds sensorRect: "+ sensorRect);

                float maxZoom = characteristics.get(characteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                Log.d(TAG, "_cameraIds maxZoom: "+ maxZoom);

                // Check if it's a logical camera with multiple physical cameras
                Set<String> physicalCameraIds = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    physicalCameraIds = characteristics.getPhysicalCameraIds();
                    Log.d(TAG, "_cameraIds physicalCameraIds: "+ physicalCameraIds);
                }
                if (physicalCameraIds != null && !physicalCameraIds.isEmpty()) {
                    // This is a logical camera with multiple physical cameras
                    System.out.println("_cameraIds Logical camera ID: " + cameraId);
                    for (String physicalCameraId : physicalCameraIds) {
                        System.out.println("_cameraIds Physical camera ID: " + physicalCameraId);
                    }
                } else {
                    // This is a single physical camera
                    System.out.println("_cameraIds Single camera ID: " + cameraId);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera("0"); // Mặc định mở camera thường
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

    private void openCamera(String cameraId) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, null);
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

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
//            openCamera(normalCameraId); // Mặc định mở camera thường
            openCamera("0"); // Mặc định mở camera thường
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
