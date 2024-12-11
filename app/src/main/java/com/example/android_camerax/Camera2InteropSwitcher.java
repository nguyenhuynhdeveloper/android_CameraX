package com.example.android_camerax;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.SizeF;

import androidx.annotation.NonNull;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class Camera2InteropSwitcher {

    private final Context context;
    private final PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector currentCameraSelector;

    public Camera2InteropSwitcher(Context context, PreviewView previewView) {
        this.context = context;
        this.previewView = previewView;
    }

    public void setupCamera() {
        // Initialize the ProcessCameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                // Start with default wide camera
                switchToWideCamera();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("Camera2InteropSwitcher", "Failed to initialize CameraProvider", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void switchToWideCamera() {
        if (cameraProvider == null) return;

        // Create a CameraSelector for the wide camera
        currentCameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        bindCameraUseCases(currentCameraSelector);
    }



    // Chuyển đổi sang camera góc siêu rộng
    public void switchToUltraWideCamera() {
        if (cameraProvider == null) return;

        // Create a CameraSelector with Camera2Interop to filter for the ultra-wide camera
        CameraSelector ultraWideCameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .addCameraFilter(cameraInfos -> {
                    for (CameraInfo cameraInfo : cameraInfos) {
                        Camera2CameraInfo camera2CameraInfo = Camera2CameraInfo.from(cameraInfo);

                        // Access Camera2 characteristics
                        Integer focalLengths = camera2CameraInfo.getCameraCharacteristic(
                                CameraCharacteristics.LENS_FACING);

                        SizeF sensorSize = camera2CameraInfo.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                        Log.d("TAG", "TAG");

//                        if (sensorSize.getWidth() >= 6.5) {
                        if (sensorSize.getWidth() <= 6.5) {
                            return Collections.singletonList(cameraInfo);
                        }

                        // Check if the camera is ultra-wide based on custom criteria
//                        if (focalLengths != null && focalLengths == CameraCharacteristics.LENS_FACING_BACK) {
//                            return Collections.singletonList(cameraInfo);
//                        }
                    }
                    return Collections.emptyList();
                })
                .build();

        bindCameraUseCases(ultraWideCameraSelector);
    }

    private void bindCameraUseCases(CameraSelector cameraSelector) {
        // Ensure all use cases are unbound before binding new ones
        cameraProvider.unbindAll();

        // Configure a Preview use case
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Bind the camera selector and preview use case to the lifecycle
        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);
    }
}
