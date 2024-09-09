package com.example.android_camerax;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExposureState;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
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


public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private float minZoom;
    private float maxZoom;

    private ImageButton flashButton;
    private  String TAG = "CameraActivity";

    // Các Button chức năng
    private ImageButton camera_switch_button, exposureButton , countdownButton, zoomButton;

    private Button button_zoom_3x;
    private Button button_zoom_1x;
    private Button button_zoom_2x;

    private ImageButton button1s , button2s, button3s ;



    private SeekBar exposureSeekBar;

    private ConstraintLayout toolBar, cameraTopLayout;



    private boolean isExpanded = false;
    int isSelectCountdown = 0 ;
    private TextView countdownTextView;

    RulerView ruler_view ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.camera_previewView);
        // Các ánh xạ view mới


        button_zoom_1x = findViewById(R.id.button_zoom_1x);
        button_zoom_2x = findViewById(R.id.button_zoom_2x);
        button_zoom_3x = findViewById(R.id.button_zoom_3x);



        button1s = findViewById(R.id.button_1s);
        button2s = findViewById(R.id.button_2s);
        button3s = findViewById(R.id.button_3s);

        // Ánh xạ view từ XML
        camera_switch_button = findViewById(R.id.camera_switch_button);
        exposureSeekBar = findViewById(R.id.exposure_seekBar);
        exposureButton = findViewById(R.id.camera_exposure_button);
        countdownButton = findViewById(R.id.camera_countdown_button);
        zoomButton = findViewById(R.id.camera_zoom_button);

        toolBar = findViewById(R.id.tool_bar);
        cameraTopLayout = findViewById(R.id.camera_toplayout);

        exposureSeekBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        countdownTextView = findViewById(R.id.countdownTextView);
        ruler_view = findViewById(R.id.ruler_view);


        // Xin quyền hỏi quyền
        if (allPermissionsGranted()) {
            startCamera(CameraSelector.LENS_FACING_BACK, 1.0f, false);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


        // Khi Click vào button phơi sáng
        exposureButton.setOnClickListener(v -> {
            // Ẩn các nút và hiển thị SeekBar


            toggleToolBarButtons(View.GONE);
            exposureSeekBar.setVisibility(View.VISIBLE);

            exposureSeekBar.setProgress(5);


        });

        // Chuyển đổi từ dp sang pixel
        float distance50dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        float buttonWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());

        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!isExpanded) {
                    Drawable drawable = ContextCompat.getDrawable(CameraActivity.this, R.drawable.camera_countdown);
                    countdownButton.setImageDrawable(drawable);

                    exposureButton.setVisibility(View.INVISIBLE);
                    zoomButton.setVisibility(View.INVISIBLE);
                    camera_switch_button.setVisibility(View.INVISIBLE);

                    switch (isSelectCountdown){
                        case 0:
                            countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.colorAccent)));
                            button1s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button2s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button3s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            break;
                        case 1:
                            countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button1s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.colorAccent)));
                            button2s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button2s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            break;
                        case 2:
                            button2s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.colorAccent)));
                            countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button1s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button3s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            break;
                        case 3:
                            button3s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.colorAccent)));
                            countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button1s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            button2s.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                            break;
                    }


                    // Hiển thị các button
                    button1s.setVisibility(View.VISIBLE);
                    button2s.setVisibility(View.VISIBLE);
                    button3s.setVisibility(View.VISIBLE);

                    // Định vị trí ban đầu của các button
                    button1s.setX(countdownButton.getX());
                    button2s.setX(countdownButton.getX());
                    button3s.setX(countdownButton.getX());

                    // Tạo các ObjectAnimator để di chuyển các button
                    ObjectAnimator moveButton1 = ObjectAnimator.ofFloat(button1s, "translationX", 0f, distance50dp +   buttonWidth);
                    ObjectAnimator moveButton2 = ObjectAnimator.ofFloat(button2s, "translationX", 0f,  distance50dp*2 + buttonWidth*2);
                    ObjectAnimator moveButton3 = ObjectAnimator.ofFloat(button3s, "translationX", 0f, distance50dp*3 + buttonWidth*3);

                    // Tạo AnimatorSet để chơi các animation tuần tự
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playSequentially(moveButton1, moveButton2, moveButton3);
                    animatorSet.setDuration(100); // thời gian di chuyển (ms)
                    animatorSet.start();
                    isExpanded = true;
                }else {
                    isSelectCountdown = 0 ;
                    countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.white)));
                    collapseButtons(button1s, button2s, button3s);
                    isExpanded = false;
                }

            }
        });

        button1s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectCountdown = 1;
                collapseButtons(button1s, button2s, button3s);
                isExpanded = false;
                Drawable drawable = ContextCompat.getDrawable(CameraActivity.this, R.drawable.camera_countdown_2s);
                countdownButton.setImageDrawable(drawable);
                countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.colorAccent)));
            }
        });
        button2s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectCountdown = 2;

                collapseButtons(button1s, button2s, button3s);
                isExpanded = false;
                Drawable drawable = ContextCompat.getDrawable(CameraActivity.this, R.drawable.camera_countdown_5s);
                countdownButton.setImageDrawable(drawable);
                countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.colorAccent)));
            }
        });
        button3s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectCountdown = 3;

                collapseButtons(button1s, button2s, button3s);
                isExpanded = false;
                Drawable drawable = ContextCompat.getDrawable(CameraActivity.this, R.drawable.camera_countdown_10s);
                countdownButton.setImageDrawable(drawable);
                countdownButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(CameraActivity.this, R.color.colorAccent)));
            }
        });

        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ruler_view.setVisibility(View.VISIBLE);
                button1s.setVisibility(View.INVISIBLE);
                button2s.setVisibility(View.INVISIBLE);
                button3s.setVisibility(View.INVISIBLE);
                exposureButton.setVisibility(View.INVISIBLE);
                zoomButton.setVisibility(View.INVISIBLE);
                camera_switch_button.setVisibility(View.INVISIBLE);
                countdownButton.setVisibility(View.INVISIBLE);
            }
        });

        // Truyền tham số minZoomLevel, maxZoomLevel, và zoomLevel từ MainActivity
        float initialZoomLevel = 1.0f; // Ví dụ: mức zoom khởi tạo
        float maxZoom = 10.0f;         // Ví dụ: mức zoom tối đa
        float minZoom = 1.0f;          // Ví dụ: mức zoom tối thiểu

        ruler_view.setMinZoomLevel(minZoom);  // Thiết lập mức zoom tối thiểu
        ruler_view.setMaxZoomLevel(maxZoom);  // Thiết lập mức zoom tối đa

        ruler_view.setZoomLevel(initialZoomLevel);  // Thiết lập mức zoom ban đầu
        // Đăng ký lắng nghe thay đổi zoom level từ RulerView
        ruler_view.setOnZoomLevelChangeListener(new RulerView.OnZoomLevelChangeListener() {
            @Override
            public void onZoomLevelChanged(float newZoomLevel) {
                // Cập nhật TextView để hiển thị mức zoom hiện tại
//                zoomLevelTextView.setText(String.format("Zoom: %.1fx", newZoomLevel));
                camera.getCameraControl().setZoomRatio(newZoomLevel);
                Log.d(TAG , "newZoomLevel: " +newZoomLevel);
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

        // Lấy giá trị zoom tối thiểu và tối đa của 1 camera
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

    // Hàm thay đổi tỷ lệ Zoom
    public void setZoomRatio( Float ratio) {
//        cameraController.setZoomRatio(ratio);
    }

    private void toggleToolBarButtons(int visibility) {
        exposureButton.setVisibility(visibility);
        countdownButton.setVisibility(visibility);
        zoomButton.setVisibility(visibility);
        camera_switch_button.setVisibility(visibility);
    }

    private void collapseButtons(ImageButton... buttons) {
        for (ImageButton button : buttons) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(button, "translationX", button.getTranslationX(), 0f);
            animator.setDuration(300);
            animator.start();
            button.setVisibility(View.INVISIBLE);
        }

        // Tạo một Handler để thực hiện hành động sau 3 giây
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exposureButton.setVisibility(View.VISIBLE);
                zoomButton.setVisibility(View.VISIBLE);
                camera_switch_button.setVisibility(View.VISIBLE); // Hiển thị nút sau 3 giây
            }
        }, 200); // 3000 milliseconds = 3 seconds
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Lấy tọa độ của sự kiện chạm
            float x = event.getX();
            float y = event.getY();

            // Kiểm tra nếu sự kiện chạm xảy ra bên ngoài SeekBar
            if (exposureSeekBar.getVisibility() == View.VISIBLE && !isPointInsideView(x, y, exposureSeekBar)) {
                exposureSeekBar.setVisibility(View.GONE);
                toggleToolBarButtons(View.VISIBLE);
            }

            if (ruler_view.getVisibility() == View.VISIBLE && !isPointInsideView(x, y, ruler_view)) {
                ruler_view.setVisibility(View.GONE);
                toggleToolBarButtons(View.VISIBLE);
            }
        }
        return super.dispatchTouchEvent(event); // Gọi hàm cha để tiếp tục xử lý các sự kiện chạm khác
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        Log.d("TAG","viewX " + String.valueOf(viewX) );
        Log.d("TAG", "viewY " + String.valueOf(viewY));

        return (x > (viewX - view.getWidth()) && x < (viewX + view.getWidth()) ) &&
                (y > (viewY - view.getHeight() ) && y < (viewY + view.getHeight() ));
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
