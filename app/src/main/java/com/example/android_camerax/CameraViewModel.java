package com.example.android_camerax;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CameraViewModel extends ViewModel {

    private MutableLiveData<Float> currentButtonZoom = new MutableLiveData<>();
    public LiveData<Float> getCurrentButtonZoom() {
        return currentButtonZoom;
    }
    public void setCurrentButtonZoom(Float value) {
        Log.d("TAG_CameraViewModel", "_setCurrentButtonZoom _currentButtonZoom: " +value );
        currentButtonZoom.setValue(value);
    }

    private MutableLiveData<Float> currentValueZoom = new MutableLiveData<>();
    public  LiveData<Float> getCurrentValueZoom () {
        return  currentValueZoom;
    }
    public  void setCurrentValueZoom (Float value ) {currentValueZoom.setValue(value);}

    private  MutableLiveData<String> cameraDeviceId = new MutableLiveData<>();
    public LiveData<String> getCameraDeviceId() {return cameraDeviceId;};
    public void setCameraDeviceId(String value ) {cameraDeviceId.setValue(value);}

    private MutableLiveData<Float> currentValueRulerView = new MutableLiveData<>(1.0f);
    public  LiveData<Float> getCurrentValueRulerView () {return currentValueRulerView ;}
    public void setCurrentValueRulerView(Float value) {
//        Log.d("TAG_CameraViewModel", "_setCurrentValueRulerView _currentValueRulerView : " + value);
        currentValueRulerView.setValue(value);}


    private MutableLiveData<Boolean> isHaveUltrawideCamera = new MutableLiveData<Boolean>(Boolean.FALSE);
    public LiveData<Boolean> getIsHaveUltrawideCamera () {return  isHaveUltrawideCamera;}
    public void setIsHaveUltrawideCamera(Boolean value) {
        isHaveUltrawideCamera.setValue(value);
    }


    private MutableLiveData<Float> valueMinZoom = new MutableLiveData<>(1.0f);
    public  LiveData<Float> getValueMinZoom () {return  valueMinZoom;}
    public  void setValueMinZoom(Float value ) { valueMinZoom.setValue(value);}


    private MutableLiveData<Float> valueMaxZoom = new MutableLiveData<>(3.0f);
    public  LiveData<Float> getValueMaxZoom () {return  valueMaxZoom;}
    public  void setValueMaxZoom(Float value ) { valueMaxZoom.setValue(value);}

}
