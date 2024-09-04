import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.android_camerax.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.camera.camera2.interop.Camera2CameraInfo;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private ActivityMainBinding binding;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ExecutorService cameraExecutor;
    private String wideAngleCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Kiểm tra quyền truy cập camera
        if (allPermissionsGranted()) {
            initializeCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void initializeCamera() {
        // Kiểm tra và hiển thị hoặc ẩn nút 0.5X
        if (checkWideAngleCamera()) {
            binding.buttonZoom05x.setVisibility(View.VISIBLE);
        } else {
            binding.buttonZoom05x.setVisibility(View.GONE);
        }

        startCamera();

        binding.buttonZoom05x.setOnClickListener(v -> switchToWideAngleLens());
        binding.buttonZoom1x.setOnClickListener(v -> setZoomRatio(1.0f));
        binding.buttonZoom2x.setOnClickListener(v -> setZoomRatio(2.0f));
    }

    private boolean checkWideAngleCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    SizeF sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);

                    if (focalLengths != null && sensorSize != null) {
                        // Check if it's a wide-angle lens
                        if (focalLengths.length > 0 && (sensorSize.getWidth() / focalLengths[0]) > 1.0) {
                            wideAngleCameraId = cameraId;
                            return true;
                        }
                    }
                }
            }
        } catch (CameraAccessException e) {
            Log.e("CameraXDemo", "Cannot access camera: " + e.getMessage());
        }
        return false;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCase(CameraSelector.DEFAULT_BACK_CAMERA);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCase(@NonNull CameraSelector cameraSelector) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setZoomRatio(float ratio) {
        if (camera != null) {
            camera.getCameraControl().setZoomRatio(ratio);
        }
    }

    private void switchToWideAngleLens() {
        if (wideAngleCameraId != null) {
            CameraSelector wideAngleSelector = new CameraSelector.Builder()
                    .addCameraFilter(cameraInfos -> {
                        List<CameraInfo> filteredList = new ArrayList<>();
                        for (CameraInfo cameraInfo : cameraInfos) {
                            try {
                                String cameraId = Camera2CameraInfo.from(cameraInfo).getCameraId();
                                if (cameraId.equals(wideAngleCameraId)) {
                                    filteredList.add(cameraInfo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return filteredList;
                    })
                    .build();

            bindCameraUseCase(wideAngleSelector);
        }
    }


    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                initializeCamera();
            } else {
                // Quyền bị từ chối
                Log.e("CameraXDemo", "Quyền truy cập camera bị từ chối");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
