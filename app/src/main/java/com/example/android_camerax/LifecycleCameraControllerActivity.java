package com.example.android_camerax;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.ImageCapture;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LifecycleCameraControllerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lifecycle_camera_controller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}


//
//package com.example.android_camerax;
//
//        import android.Manifest;
//        import android.content.DialogInterface;
//        import android.content.pm.PackageManager;
//        import android.hardware.camera2.CameraCharacteristics;
//        import android.os.Bundle;
//        import android.util.Log;
//        import android.view.View;
//        import android.widget.Button;
//        import android.widget.ImageButton;
//        import android.widget.ImageView;
//        import android.widget.Toast;
//
//        import androidx.annotation.NonNull;
//        import androidx.annotation.OptIn;
//        import androidx.appcompat.app.AppCompatActivity;
//
//
//
//
//        import androidx.camera.core.AspectRatio;
//        import androidx.camera.core.CameraControl;
//        import androidx.camera.core.CameraInfo;
//        import androidx.camera.core.CameraSelector;
//        import androidx.camera.core.ExposureState;
//        import androidx.camera.core.ImageCapture;
//        import androidx.camera.core.ImageCaptureException;
//        import androidx.camera.core.Preview;
//        import androidx.camera.core.ResolutionInfo;
//        import androidx.camera.core.SurfaceRequest;
//        import androidx.camera.core.processing.SurfaceProcessorNode;
//        import androidx.camera.view.CameraController;
//        import androidx.camera.view.LifecycleCameraController;
//        import androidx.camera.view.PreviewView;
//        import androidx.camera.view.transform.OutputTransform;
//
//        import androidx.constraintlayout.widget.ConstraintLayout;
//        import androidx.core.app.ActivityCompat;
//        import androidx.core.content.ContextCompat;
//
//
//        import java.util.concurrent.ExecutionException;
//
//public class MainActivity extends AppCompatActivity {
//    private static final int REQUEST_CODE_PERMISSIONS = 10;
//    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
//
//
//    private PreviewView previewView;
//    private ConstraintLayout topLayout;
//
//    private ImageView photoPreview;
//
//
//    /** カメラ関連 */
//    /** Liên quan đến máy ảnh */
//    private LifecycleCameraController cameraController;   // Triển khai bằng cách này thì sẵn có tính năng Pich to Zoom mặc định
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        previewView = findViewById(R.id.previewView);
//        Button buttonWideAngle = findViewById(R.id.buttonWideAngle);
//        Button buttonNormal = findViewById(R.id.buttonNormal);
//
//        if (allPermissionsGranted()) {
//            startCameraController();
//        } else {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
//        }
//
//
//    }
//
//
//    private void startCameraController() {
//
//
//        cameraController = new LifecycleCameraController(getBaseContext());
//
////        CameraSelector selector =
////                new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
////        cameraController.setCameraSelector(selector);
//
//
//
//        cameraController.setImageCaptureFlashMode(ImageCapture.FLASH_MODE_AUTO);
//
//
////        if(withHashMode) {
////            //改ざん検知機能付の場合、サイズを小さく
////            Size size = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
////                    new Size(1600, 1200) : new Size(1200, 1600);
////            cameraController.setImageCaptureTargetSize(new CameraController.OutputSize(size));
////            cameraController.setImageAnalysisTargetSize(new CameraController.OutputSize(size));
////
////        } else {
//        cameraController.setImageCaptureTargetSize(new CameraController.OutputSize(AspectRatio.RATIO_4_3));
//        cameraController.setImageAnalysisTargetSize(new CameraController.OutputSize(AspectRatio.RATIO_4_3));
////        }
//
//
//        cameraController.bindToLifecycle(this);
//
//        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
//
//
//        previewView.setController(cameraController);
//
//
//
//
//
//
//
//
//
//
//    }
//
//
//
//
//    private boolean allPermissionsGranted() {
//        for (String permission : REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCameraController();
//            } else {
//                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }
//}
