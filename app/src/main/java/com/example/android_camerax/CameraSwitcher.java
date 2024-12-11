package com.example.android_camerax;

import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.Camera2Interop;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraSwitcher {

    private Context context;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;
    private CameraSelector currentCameraSelector;
    private Preview preview;
    private Camera camera;
    private ImageCapture imageCapture;

    public CameraSwitcher(Context context, PreviewView previewView) {
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void setupCamera(LifecycleOwner lifecycleOwner) {
        // Initialize CameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Default to wide camera
                switchToWideCamera(lifecycleOwner);

            } catch (Exception e) {
                Log.e("CameraSwitcher", "Failed to get camera provider", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases(LifecycleOwner lifecycleOwner, CameraSelector cameraSelector) {
        if (cameraProvider == null) return;

        // Unbind all use cases
        cameraProvider.unbindAll();

        // Configure Preview
        preview = new Preview.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Configure ImageCapture
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        // Bind use cases to lifecycle
        camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture);
    }

    public void switchToWideCamera(LifecycleOwner lifecycleOwner) {
        currentCameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Default to back camera
                .build();
        bindCameraUseCases(lifecycleOwner, currentCameraSelector);
    }

    public void switchToUltraWideCamera(LifecycleOwner lifecycleOwner) {

        currentCameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Ultra-wide is also back-facing
                .addCameraFilter(cameraInfos -> {

                    // Filter for ultra-wide cameras (by field-of-view or other characteristics)
                    for (CameraInfo cameraInfo : cameraInfos) {
//                        Integer fieldOfView = cameraInfo.getCameraCharacteristic(CameraCharacteristics.LENS_FACING);
                        @SuppressLint("RestrictedApi") Integer fieldOfView = cameraInfo.getCameraSelector().getLensFacing();

                        if (fieldOfView != null && fieldOfView.equals(CameraCharacteristics.LENS_FACING_BACK)) {
                            return Collections.singletonList(cameraInfo); // Return the ultra-wide camera
                        }
                    }
                    return Collections.emptyList(); // Fallback if no ultra-wide camera
                })
                .build();
        bindCameraUseCases(lifecycleOwner, currentCameraSelector);
    }

    public void takePicture(ImageCapture.OnImageSavedCallback callback) {
        if (imageCapture == null) return;

        // Create file to save image
        File photoFile = new File(context.getExternalFilesDir(null), "photo.jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor, callback);
    }

    public void release() {
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
