package com.example.android_camerax;



//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.lifecycle.LifecycleOwner;
//
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final String TAG = "CameraXDemo";
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    private PreviewView previewView;
//    private ExecutorService cameraExecutor;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        previewView = findViewById(R.id.previewView);
//
//        // Khởi tạo executor cho CameraX
//        cameraExecutor = Executors.newSingleThreadExecutor();
//
//        if (allPermissionsGranted()) {
//            startCamera();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CAMERA},
//                    REQUEST_CAMERA_PERMISSION);
//        }
//    }
//
//    private void startCamera() {
////        final ProcessCameraProvider cameraProviderFuture = ProcessCameraProvider.getInstance(this);   // ChatGPT không đúng
//
//        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(MainActivity.this);
//
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//
//                // Xác định CameraSelector (lựa chọn camera trước hoặc sau)
//                CameraSelector cameraSelector = new CameraSelector.Builder()
//                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                        .build();
//
//                // Tạo một Preview
//                Preview preview = new Preview.Builder()
//                        .build();
//
//                // Kết nối PreviewView với Preview
//                preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
//                // Cài đặt LifecycleOwner cho CameraX
//                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
//
//            } catch (ExecutionException | InterruptedException e) {
//                Log.e(TAG, "CameraX initialization failed.", e);
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private boolean allPermissionsGranted() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startCamera();
//            } else {
//                Toast.makeText(this, "Camera permission is needed to show camera preview.", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }
//}

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

//package com.example.android_camerax;

        import android.Manifest;
        import android.content.DialogInterface;
        import android.content.pm.PackageManager;
        import android.hardware.camera2.CameraCharacteristics;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.ImageView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.annotation.OptIn;
        import androidx.appcompat.app.AppCompatActivity;




        import androidx.camera.core.AspectRatio;
        import androidx.camera.core.CameraControl;
        import androidx.camera.core.CameraInfo;
        import androidx.camera.core.CameraSelector;
        import androidx.camera.core.ExposureState;
        import androidx.camera.core.ImageCapture;
        import androidx.camera.core.ImageCaptureException;
        import androidx.camera.core.Preview;
        import androidx.camera.core.ResolutionInfo;
        import androidx.camera.core.SurfaceRequest;
        import androidx.camera.core.processing.SurfaceProcessorNode;
        import androidx.camera.view.CameraController;
        import androidx.camera.view.LifecycleCameraController;
        import androidx.camera.view.PreviewView;
        import androidx.camera.view.transform.OutputTransform;

        import androidx.constraintlayout.widget.ConstraintLayout;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;


        import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};


    private PreviewView previewView;
    private ConstraintLayout topLayout;

    private ImageView photoPreview;


    /** カメラ関連 */
    /** Liên quan đến máy ảnh */
    private LifecycleCameraController cameraController;   // Triển khai bằng cách này thì sẵn có tính năng Pich to Zoom mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        Button buttonWideAngle = findViewById(R.id.buttonWideAngle);
        Button buttonNormal = findViewById(R.id.buttonNormal);

        if (allPermissionsGranted()) {
            startCameraController();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }


    private void startCameraController() {


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

        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);


        previewView.setController(cameraController);










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
                startCameraController();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}



