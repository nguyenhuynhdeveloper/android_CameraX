package com.example.android_camerax;


// Sử dụng camera2
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String cameraId : cameraIdList) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                // Lấy ra các thông số của camera
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                float[] apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                int hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

                // In ra các thông số
                System.out.println("Wide_Angle_Camera Camera ID: " + cameraId);
                System.out.println("Wide_Angle_Camera Lens Facing: " + lensFacing);
                System.out.println("Wide_Angle_Camera Focal Lengths: " + Arrays.toString(focalLengths));
                System.out.println("Wide_Angle_Camera Apertures: " + Arrays.toString(apertures));
                System.out.println("Wide_Angle_Camera Sensor Orientation: " + sensorOrientation);
                System.out.println("Wide_Angle_Camera Hardware Level: " + hardwareLevel);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
