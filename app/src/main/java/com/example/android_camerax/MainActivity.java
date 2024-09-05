package com.example.android_camerax;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.impl.CameraInfoInternal;
import androidx.camera.camera2.interop.Camera2CameraInfo;

import androidx.camera.core.CameraInfo;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.Observer;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private float minZoom;
    private float maxZoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        Button buttonWideAngle = findViewById(R.id.buttonWideAngle);
        Button buttonNormal = findViewById(R.id.buttonNormal);

        if (allPermissionsGranted()) {
            startCamera(CameraSelector.LENS_FACING_BACK, 1.0f, false);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        buttonWideAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera(CameraSelector.LENS_FACING_BACK, 0.5f, true);
            }
        });

        buttonNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera(CameraSelector.LENS_FACING_BACK, 1.0f, false);
            }
        });
    }

    private void startCamera(int lensFacing, float zoomRatio, boolean useWideAngle) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider, lensFacing, zoomRatio, useWideAngle);
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors (including cancellation) here.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider, int lensFacing, float zoomRatio, boolean useWideAngle) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector;

      {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build();
        }



        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        camera.getCameraControl().setZoomRatio(zoomRatio);

        // Lấy giá trị zoom tối thiểu và tối đa
        LiveData<ZoomState> zoomStateLiveData = camera.getCameraInfo().getZoomState();
        zoomStateLiveData.observe(this, new Observer<ZoomState>() {
            @Override
            public void onChanged(ZoomState zoomState) {
                minZoom = zoomState.getMinZoomRatio();
                maxZoom = zoomState.getMaxZoomRatio();

                // Log hoặc hiển thị giá trị
                Log.d("CameraXApp", "Min Zoom: " + minZoom);
                Log.d("CameraXApp", "Max Zoom: " + maxZoom);
            }
        });
    }



    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(CameraSelector.LENS_FACING_BACK, 1.0f, false);
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
