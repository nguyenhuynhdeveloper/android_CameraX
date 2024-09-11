package com.example.android_camerax;



import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.hardware.camera2.CameraManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private Button captureButton;
    private ImageCapture imageCapture;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private LifecycleCameraController cameraController;
    private CameraInfo cameraInfo;
    private CameraManager cameraManager;
    private  String TAG = "Wide_Angle_Camera";

    private String wideCameraId;
    private String normalCameraId;
    private String teleCameraId;
    private String frontCameraId;
    private String cameraId;

    private CameraDevice cameraDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.captureButton);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Kiểm tra và yêu cầu quyền camera
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
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
        } catch ( CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, bắt đầu camera
                startCamera();
            } else {
                // Quyền bị từ chối
                Toast.makeText(this, "Camera permission is required to use this app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        cameraController = new LifecycleCameraController(getBaseContext());

//        CameraSelector selector =
//                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
//        cameraController.setCameraSelector(selector);



        cameraController.setImageCaptureFlashMode(ImageCapture.FLASH_MODE_AUTO);


//        if(withHashMode) {
//            //改ざん検知機能付の場合、サイズを小さく
//            Size size = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
//                    new Size(1600, 1200) : new Size(1200, 1600);
//            cameraController.setImageCaptureTargetSize(new CameraController.OutputSize(size));
//            cameraController.setImageAnalysisTargetSize(new CameraController.OutputSize(size));
//
//        } else {
        cameraController.setImageCaptureTargetSize(new CameraController.OutputSize(AspectRatio.RATIO_4_3));
        cameraController.setImageAnalysisTargetSize(new CameraController.OutputSize(AspectRatio.RATIO_4_3));
//        }


        cameraController.bindToLifecycle(this);
//        cameraController.setPinchToZoomEnabled(false);
        cameraInfo = cameraController.getCameraInfo();
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);


        previewView.setController(cameraController);

    }


    // Hàm tách riêng
//    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
//        Preview preview = new Preview.Builder().build();
//
//        imageCapture = new ImageCapture.Builder().build();
//
//        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
//
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
//        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
//    }

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
//            createCameraPreview();
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

    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }

        File photoFile = new File(getExternalFilesDir(null), "photo.jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String msg = "Photo capture succeeded: " + photoFile.getAbsolutePath();
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d("CameraXApp", msg);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        String msg = "Photo capture failed: " + exception.getMessage();
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.e("CameraXApp", msg);
                    }
                }
        );
    }
}
