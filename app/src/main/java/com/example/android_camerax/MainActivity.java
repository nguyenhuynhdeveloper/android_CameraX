package com.example.android_camerax;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


import android.os.Handler;
import android.os.HandlerThread;

public class MainActivity extends AppCompatActivity {

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private TextureView textureView;
    private Button btnCapture;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private String TAG = "Capture";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        btnCapture = findViewById(R.id.btnCapture);

        textureView.setSurfaceTextureListener(textureListener); // Trong hàm này, chúng ta thiết lập giao diện người dùng và gán SurfaceTextureListener cho TextureView.
//  Gán SurfaceTextureListener cho TextureView để theo dõi các sự kiện liên quan đến SurfaceTexture.
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
// Kiểm tra quyền xem nếu chưa có quyền thì sẽ hỏi lại quyền
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }
    }

    // Đây là một listener để theo dõi các sự kiện liên quan đến SurfaceTexture của TextureView.
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        // Được gọi khi SurfaceTexture đã sẵn sàng để sử dụng. Chúng ta mở camera trong hàm này.
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
// Được gọi khi kích thước của SurfaceTexture thay đổi.
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        // Được gọi khi SurfaceTexture bị phá hủy.
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
// Được gọi khi SurfaceTexture được cập nhật.
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    // Đây là một callback để theo dõi trạng thái của CameraDevice.
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        // Được gọi khi camera đã được mở thành công. Chúng ta lưu lại CameraDevice và tạo preview camera.
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;  // Đây là nơi tạo ra đối tượng camẻaDevice
            createCameraPreview();
        }

        // Được gọi khi camera bị ngắt kết nối. Chúng ta đóng camera trong hàm này.
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        // Được gọi khi có lỗi xảy ra với camera. Chúng ta đóng camera và đặt cameraDevice về null.
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    // Hàm này mở camera. Nó lấy CameraManager, xác định camera ID, kiểm tra quyền truy cập camera và mở camera.
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            manager.openCamera(cameraId, stateCallback, null);   // Mở camera bằng cách gọi
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Hàm này tạo preview cho camera. Nó thiết lập SurfaceTexture, tạo CaptureRequest và tạo phiên chụp (CameraCaptureSession).
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture(); // Lấy SurfaceTexture từ TextureView và thiết lập kích thước buffer mặc định.
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight()); // Gán kích thứớc texture
            Surface surface = new Surface(texture);  // Tạo Surface từ SurfaceTexture.
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface); //  Tạo CaptureRequest cho preview và thêm Surface vào CaptureRequest.

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    cameraCaptureSession = session;  // Tạo phiên chụp camera
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Hàm này cập nhật preview của camera.
    // Nó thiết lập chế độ điều khiển của CaptureRequest và bắt đầu lặp lại yêu cầu chụp (setRepeatingRequest).
    private void updatePreview() {
        if (cameraDevice == null) return;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);  // Thiết lập chế độ điều khiển của CaptureRequest là tự động.
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null); // Bắt đầu lặp lại yêu cầu chụp (setRepeatingRequest) để cập nhật preview liên tục.
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

// Hàm này chụp ảnh. Nó thiết lập ImageReader, tạo CaptureRequest cho ảnh tĩnh, và tạo phiên chụp để chụp ảnh.
    private void takePicture() {
        if (cameraDevice == null) return;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
           // Lấy CameraManager và CameraCharacteristics
            // Lấy thuộc tính của camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());

            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

//            Thiết lập kích thước ảnh
            // Hàm thiết lập kích thước ảnh mặc định là 640x480. Nếu có kích thước JPEG được hỗ trợ, nó sẽ sử dụng kích thước đầu tiên trong danh sách.
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            // Camera2 khi chụp ảnh sẽ đưa ra định dạng ImageReader
            // Hàm tạo một ImageReader để nhận ảnh JPEG và thiết lập các Surface đầu ra
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);  // Tạo ImageReader với kích thước ảnh xác định.

            List<Surface> outputSurfaces = new ArrayList<>(2);  // Biến mấu chốt: Sẽ phục vụ cho cameraDevice.createCaptureSession
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));


            //  Tạo một CaptureRequest.Builder để chụp ảnh và thêm Surface của ImageReader vào yêu cầu chụp.
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

//            Thiết lập hướng ảnh
            // Hàm lấy hướng của màn hình và thiết lập hướng cho ảnh chụp.
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            // Tạo 1 đường dẫn file để lưu hình ảnh
            // Hàm tạo một file mới trong bộ nhớ trong để lưu ảnh chụp.
            file = new File(getExternalFilesDir(null) + "/" + UUID.randomUUID().toString() + ".jpg");

            Log.d(TAG, "file :" +file);

            // Lưu ảnh khi nó có sẵn
            // Tạo 1 biến lắng nghe sự đọc ảnh , biến này sẽ được gán vào render :ImageReader ở trên
            // Đây là một listener để theo dõi khi ảnh đã sẵn sàng.

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

                // Được gọi khi ảnh đã sẵn sàng. Nó lấy ảnh từ ImageReader, đọc dữ liệu từ ảnh và lưu lại.
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                // Hàm lưu hình ảnh vào file đường dẫn  ---  Lưu dữ liệu ảnh vào file.
                private void save(byte[] bytes) {
                    OutputStream output = null;  // Mở OutputStream và ghi dữ liệu ảnh vào file.
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (output != null) {
                                output.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            //Hàm thiết lập một OnImageAvailableListener để xử lý khi ảnh đã sẵn sàng.
            // Nó đọc dữ liệu từ ImageReader, lưu dữ liệu vào file và đóng Image.
            reader.setOnImageAvailableListener(readerListener, null);

            // Tạo biến dể phục vụ hàm lắng nghe khi thao tác chụp ảnh - Biến mấu chốt: --->  sẽ phục vụ cho cameraDevice.createCaptureSession
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Saved: " + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();

                }
            };

            // Tạo 1 phiên chụp ảnh - chụp 1 ảnh dạng outputSurfaces
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
//                        Phương thức này gửi một yêu cầu chụp ảnh đến camera và sử dụng captureListener để nhận thông báo khi yêu cầu hoàn tất.
                        session.capture(captureBuilder.build(), captureListener, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

   //  // Hàm này được gọi khi Activity được tiếp tục. Nó khởi động luồng nền và mở camera nếu TextureView đã sẵn sàng.
//    @Override
//    protected void onResume() {
//        super.onResume();
//        startBackgroundThread();  // Khởi động luồng nền (startBackgroundThread()).
    // // Kiểm tra nếu TextureView đã sẵn sàng thì mở camera, nếu không thì gán SurfaceTextureListener.
//        if (textureView.isAvailable()) {  // Nếu  textureView đã sẵn sàng thì mở camera
//            openCamera();
//        } else {
//            textureView.setSurfaceTextureListener(textureListener);  // nếu chưa có thì khởi tạo lại _textureListener trong biến đó đã có mở camera rồi
//        }
//    }

    // Hàm này được gọi khi Activity bị tạm dừng. Nó dừng luồng nền.
    // Khi Activity bị tạm dừng, bạn nên đóng phiên camera để tránh các lỗi liên quan đến phiên bị đóng.
//    @Override
//    protected void onPause() {
//        stopBackgroundThread();  // dừng luồng nền (stopBackgroundThread()
//        super.onPause();  // Đóng camera (closeCamera())
//    }

    // // Hàm này khởi động luồng nền để xử lý các tác vụ liên quan đến camera.
//// Tạo và khởi động HandlerThread và Handler cho luồng nền.
//    private void startBackgroundThread() {
//        backgroundThread = new HandlerThread("Camera Background");
//        backgroundThread.start();
//        backgroundHandler = new Handler(backgroundThread.getLooper());
//    }

   ////  Hàm này dừng luồng nền. Dừng HandlerThread một cách an toàn và giải phóng tài nguyên.
//    private void stopBackgroundThread() {
//        backgroundThread.quitSafely();
//        try {
//            backgroundThread.join();
//            backgroundThread = null;
//            backgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}



// Tổng kết
//textureListener  -->  cho textureView.setSurfaceTextureListener(textureListener);
//
//
//openCamera --> cho textureListener
//
//
//updatePreview  -> createCameraPreview  -> stateCallback -> openCamera
