package com.example.android_camerax;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Uri imageUri;
    private ImageView imageView;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button captureButton = findViewById(R.id.capture_button);
        imageView = findViewById(R.id.captured_image);

        captureButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                Log.d(TAG, "front _onCreate _setOnClickListener -> _openCamera run: ");
                openCamera();
            } else {
                requestPermissions();
            }
        });

        Log.d(TAG, "_onCreate run: ");
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, PERMISSION_REQUEST_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            imageUri = createImageUri();
            Log.d(TAG, "front _openCamera _imageUri: "+ imageUri);
            if (imageUri != null) {

                // Run ok
                Log.d(TAG, "front _openCamera _imageUri != null : "+ imageUri);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                // ------------

//                PackageManager packageManager = getPackageManager();
//                if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
////                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
//                } else {
//                    Toast.makeText(this, "Thiết bị không hỗ trợ camera trước", Toast.LENGTH_SHORT).show();
//                }

            }
        }
    }

    @Nullable
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "photo_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(this, "Failed to create image URI", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "front _createImageUri _uri: "+ uri);
        return uri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG , "front _onActivityResult _imageUri: "+ imageUri);

            if (imageUri != null) {
                // Hiển thị ảnh đã chụp
                Log.d(TAG , "front _onActivityResult _imageUri: "+ imageUri);
                imageView.setImageURI(imageUri);

                // Lưu ảnh vào trong bộ nhớ --- điện thoại thì lưu ảnh thành công
                saveImage();
            }
        }
    }

    private void saveImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(imageUri, values, null, null);
            Log.d(TAG, "front _saveImage run if: ");
        } else {
            try {
                Log.d(TAG, "front _saveImage run _imageUri: "+ imageUri);
                OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
