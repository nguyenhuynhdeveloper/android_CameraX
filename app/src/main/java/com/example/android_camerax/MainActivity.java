package com.example.android_camerax;




import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CameraXDemo";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);

        // Khởi tạo executor cho CameraX
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void startCamera() {
//        final ProcessCameraProvider cameraProviderFuture = ProcessCameraProvider.getInstance(this);   // ChatGPT không đúng

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(MainActivity.this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Xác định CameraSelector (lựa chọn camera trước hoặc sau)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Tạo một Preview
                Preview preview = new Preview.Builder()
                        .build();

                // Kết nối PreviewView với Preview
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Cài đặt LifecycleOwner cho CameraX
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "CameraX initialization failed.", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is needed to show camera preview.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}

// Nếu code bằng kotlin
// package com.example.android_camerax
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.hardware.camera2.CameraCharacteristics
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.camera2.interop.Camera2CameraInfo
//import androidx.camera.core.Camera
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import java.util.concurrent.ExecutionException
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//
//
//class MainActivity : AppCompatActivity() {
//    private var previewView: PreviewView? = null
//    private var cameraExecutor: ExecutorService? = null
//    private lateinit var camera: Camera
//    private lateinit var buttonWideAngle:Button
//    private  lateinit var  buttonNormal:Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        previewView = findViewById(R.id.previewView)
//        buttonWideAngle = findViewById(R.id.buttonWideAngle)
//        buttonNormal = findViewById(R.id.buttonNormal)
//
//        // Khởi tạo executor cho CameraX
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
////        buttonWideAngle.setOnClickListener {
////            camera?.let { it1 -> setZoomRatio(it1, 0.5f) }
////        }
////
////        buttonNormal.setOnClickListener {
////            camera?.let { it1 -> setZoomRatio(it1, 3f) }
////        }
//
//
//
//        if (allPermissionsGranted()) {
////            startCamera()
//            startWideAngleCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.CAMERA),
//                REQUEST_CAMERA_PERMISSION
//            )
//        }
//    }
//
////    private fun startCamera() {
////        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
////        cameraProviderFuture.addListener({
////            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
////
////            val preview = Preview.Builder().build().also {
////                it.setSurfaceProvider(previewView!!.surfaceProvider)
////            }
////
////            val cameraSelector = CameraSelector.Builder()
////                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
////                .build()
////
////            val camera = cameraProvider.bindToLifecycle(
////                this, cameraSelector, preview
////            )
////        }, ContextCompat.getMainExecutor(this))
////    }
//
//
////    private fun setZoomRatio(camera: Camera, zoomRatio: Float) {
////        val cameraControl = camera.cameraControl
////        val zoomState = camera.cameraInfo.zoomState.value
////
////        val newZoomRatio = zoomState?.zoomRatio?.times(zoomRatio)
////        if (newZoomRatio != null) {
////            cameraControl.setZoomRatio(newZoomRatio)
////        }
////    }
//
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView!!.surfaceProvider)
//            }
//            val cameraSelector = CameraSelector.Builder()
//                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                .build()
//            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//
//    private fun selectWideAngleCamera(cameraProvider: ProcessCameraProvider): CameraSelector {
//        val cameraInfos = cameraProvider.availableCameraInfos
////        for (cameraInfo in cameraInfos) {
////            val characteristics = Camera2CameraInfo.from(cameraInfo).getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
////            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
////            if (focalLengths != null && focalLengths.any { it < 2.0f }) {
////                return CameraSelector.Builder()
////                    .addCameraFilter { listOf(cameraInfo) }
////                    .build()
////            }
////        }
//        return CameraSelector.
//    }
//
//    private fun startWideAngleCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//            val cameraSelector = selectWideAngleCamera(cameraProvider)
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView!!.surfaceProvider)
//            }
//            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//
//    private fun allPermissionsGranted(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startCamera()
//            } else {
//                Toast.makeText(
//                    this,
//                    "Camera permission is needed to show camera preview.",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraExecutor!!.shutdown()
//    }
//
//    companion object {
//        private const val TAG = "CameraXDemo"
//        private const val REQUEST_CAMERA_PERMISSION = 200
//    }
//}

