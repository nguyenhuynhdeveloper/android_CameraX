package com.example.android_camerax;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProcessCameraProviderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_process_camera_provider);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

// ------------------------------------------------------------------
//package com.example.cameraxapp;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.Camera;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageCapture;
//import androidx.camera.core.ImageCaptureException;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.io.File;
//import java.util.concurrent.ExecutionException;
//
//public class MainActivity extends AppCompatActivity {
//
//    private PreviewView previewView;
//    private Button captureButton;
//    private ImageCapture imageCapture;
//
//    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        previewView = findViewById(R.id.previewView);
//        captureButton = findViewById(R.id.captureButton);
//
//        // Kiểm tra và yêu cầu quyền camera
//        if (checkCameraPermission()) {
//            startCamera();
//        } else {
//            requestCameraPermission();
//        }
//
//        captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                takePhoto();
//            }
//        });
//    }
//
//    private boolean checkCameraPermission() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestCameraPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Quyền đã được cấp, bắt đầu camera
//                startCamera();
//            } else {
//                // Quyền bị từ chối
//                Toast.makeText(this, "Camera permission is required to use this app", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                bindPreview(cameraProvider);
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//
//    // Hàm tách riêng
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
//
//    private void takePhoto() {
//        if (imageCapture == null) {
//            return;
//        }
//
//        File photoFile = new File(getExternalFilesDir(null), "photo.jpg");
//
//        ImageCapture.OutputFileOptions outputFileOptions =
//                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
//
//        imageCapture.takePicture(
//                outputFileOptions,
//                ContextCompat.getMainExecutor(this),
//                new ImageCapture.OnImageSavedCallback() {
//                    @Override
//                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                        String msg = "Photo capture succeeded: " + photoFile.getAbsolutePath();
//                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
//                        Log.d("CameraXApp", msg);
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCaptureException exception) {
//                        String msg = "Photo capture failed: " + exception.getMessage();
//                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
//                        Log.e("CameraXApp", msg);
//                    }
//                }
//        );
//    }
//}

// -------------------------------------------------------------------------------------------------

