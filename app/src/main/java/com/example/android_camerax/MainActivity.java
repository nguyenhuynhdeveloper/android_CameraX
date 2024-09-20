package com.example.android_camerax;



import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Set<Set<String>> cameraList = cameraManager.getConcurrentCameraIds();
                System.out.println("Wide_Angle_Camera cameraList: " + cameraList);
            }

            System.out.println("Wide_Angle_Camera cameraIdList: " + Arrays.toString(cameraIdList));
            for (String cameraId : cameraIdList) {



                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    CameraCharacteristics.Key<Integer> multi =    CameraCharacteristics.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE;

//                    System.out.println("Wide_Angle_Camera Camera multi: " + Arrays.toString(multi));
                    System.out.println("Wide_Angle_Camera Camera multi: " + multi.toString());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Integer syncType = characteristics.get(CameraCharacteristics.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE);

                    if (syncType != null) {
                        switch (syncType) {
                            case CameraCharacteristics.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE_APPROXIMATE:
                                Log.d("TAG", "Camera ID " + cameraId + " supports multi-camera with APPROXIMATE sync.");
                                break;
                            case CameraCharacteristics.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE_CALIBRATED:
                                Log.d("TAG", "Camera ID " + cameraId + " supports multi-camera with CALIBRATED sync.");
                                break;
                            default:
                                Log.d("TAG", "Camera ID " + cameraId + " does not support logical multi-camera.");
                                break;
                        }
                    } else {
                        Log.d("TAG", "Camera ID " + cameraId + " does not support multi-camera or sync type is undefined.");
                    }
                } else {
                    Log.d("TAG", "Multi-camera support is only available on Android P or higher.");
                }

                System.out.println("Wide_Angle_Camera Camera ID: " + cameraId);
                System.out.println("Wide_Angle_Camera lensFacing: " + lensFacing);
                System.out.println("Wide_Angle_Camera focalLengths: " + Arrays.toString(focalLengths));

                // Kiểm tra nếu tiêu cự lớn hơn một giá trị nhất định (ví dụ: 50mm) thì đó là camera telephoto
                if (focalLengths != null && focalLengths.length > 0) {
                    for (float focalLength : focalLengths) {
                        if (focalLength > 50) { // Giá trị này có thể thay đổi tùy thuộc vào thiết bị
                            // In ra các thông số của camera telephoto
                            int lensFacing_1 = characteristics.get(CameraCharacteristics.LENS_FACING);
                            float[] apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                            int hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

                            System.out.println("Wide_Angle_Camera Camera ID: " + cameraId);
                            System.out.println("Wide_Angle_Camera Lens Facing: " + lensFacing_1);
                            System.out.println("Wide_Angle_Camera Focal Lengths: " + Arrays.toString(focalLengths));
                            System.out.println("Wide_Angle_Camera Apertures: " + Arrays.toString(apertures));
                            System.out.println("Wide_Angle_Camera Sensor Orientation: " + sensorOrientation);
                            System.out.println("Wide_Angle_Camera Hardware Level: " + hardwareLevel);
                        }
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
