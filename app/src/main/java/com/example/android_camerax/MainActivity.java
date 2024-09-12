package com.example.android_camerax;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        Button buttonWideCamera = findViewById(R.id.buttonWideCamera);
        Button buttonNormalCamera = findViewById(R.id.buttonNormalCamera);
        Button buttonTeleCamera = findViewById(R.id.buttonTeleCamera);
        Button buttonFrontCamera = findViewById(R.id.buttonFrontCamera);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        textureView.setSurfaceTextureListener(textureListener);

        buttonWideCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                switchCamera("0");
                switchCamera(wideCameraId);
            }
        });

        buttonNormalCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                switchCamera("1");
                switchCamera(normalCameraId);

            }
        });

        buttonTeleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                switchCamera("2");
                switchCamera(teleCameraId);

            }
        });

        buttonFrontCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                switchCamera("3");
                switchCamera(frontCameraId);

            }
        });

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

                Log.d(TAG, "cameraId: " +cameraId);
                Log.d(TAG, "lensFacing: " +lensFacing);
                Log.d(TAG, "focalLengths: " + Arrays.toString(focalLengths));

                if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId; // Camera trước
                } else if (focalLengths != null && focalLengths.length > 0) {
                    if (focalLengths[0] < 2.0) {
                        wideCameraId = cameraId; // Camera góc rộng
                    } else if (focalLengths[0] > 4.0) {
                        teleCameraId = cameraId; // Camera tele
                    } else {
                        normalCameraId = cameraId; // Camera thường
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(teleCameraId); // Mặc định mở camera thường
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
