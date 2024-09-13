

// khi chụp ảnh 

private void captureStillPicture() {
    try {
        if (cameraDevice == null) return;

        // Cấu hình chế độ chụp ảnh
        final CaptureRequest.Builder captureBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());

        // Thiết lập chế độ phơi sáng tự động
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        // Bật flash nếu trạng thái flash khi chụp ảnh được bật
        if (isFlashOnWhenCapture) {
            captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);  // Nháy flash khi chụp
        } else {
            captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);  // Không bật flash
        }

        // Chụp ảnh với các cài đặt trên
        cameraCaptureSessions.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Log.d(TAG, "Picture taken");
                createCameraPreview();  // Trở về chế độ preview sau khi chụp
            }
        }, null);
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
}
