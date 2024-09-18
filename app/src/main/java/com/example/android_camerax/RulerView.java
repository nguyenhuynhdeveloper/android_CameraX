package com.example.android_camerax;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.OverScroller;
import android.widget.Scroller;

public class RulerView extends View {
    private Paint linePaint, textPaint, textPaintVerticalArrow, linePaintVerticalArrow;
    private float zoomLevel = 1.0f; // Mức zoom hiện tại
    private float maxZoomLevel = 10.0f; // Mức zoom tối đa
    private float minZoomLevel = 0.5f; // Mức zoom tối thiểu
    private int tickSpacing = 200; // Khoảng cách giữa các tick marks
    private float subMarks = 9;    // Thể hiện ví trị Zoom hiện tại


    private float offsetX = 0;    // Thể hiện ví trị Zoom hiện tại
    private float degree = 0;

    private Scroller scroller;
    private VelocityTracker velocityTracker = null;
    private OverScroller overScroller;

    private String TAG = "Ruler_RulerView";


    // Interface listener cho sự thay đổi của zoom level
    public interface OnZoomLevelChangeListener {
        void onZoomLevelChanged(float newZoomLevel);
    }

    // Biến để giữ listener
    private OnZoomLevelChangeListener zoomLevelChangeListener;

    // Phương thức để MainActivity hoặc class khác thiết lập listener
    public void setOnZoomLevelChangeListener(OnZoomLevelChangeListener listener) {
        this.zoomLevelChangeListener = listener;
    }

    public RulerView(Context context) {
        super(context);
        init(context);
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(3);

        linePaintVerticalArrow = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaintVerticalArrow.setColor(Color.YELLOW);
        linePaintVerticalArrow.setStrokeWidth(3);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaintVerticalArrow = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaintVerticalArrow.setColor(Color.WHITE);
        textPaintVerticalArrow.setTextSize(32);
        textPaintVerticalArrow.setTextAlign(Paint.Align.CENTER);

        scroller = new Scroller(context);
        overScroller = new OverScroller(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // use scroller
        if (scroller.computeScrollOffset()) {
            offsetX = scroller.getCurrX();
            postInvalidateOnAnimation();
        }

        int width = getWidth();
        int height = getHeight();

        int centerX = width / 2;
        int centerY = height / 2;

        // Cập nhật khoảng vẽ thước kẻ từ minZoomLevel đến maxZoomLevel
        int totalRange = (int) ((maxZoomLevel - minZoomLevel) * tickSpacing);
        offsetX = Math.max(-totalRange + width / 2, Math.min(width / 2, offsetX));

        Log.d(TAG, " onDraw offsetX: " + String.valueOf(offsetX));

        float prevI = 0;
        if (minZoomLevel > 1.0f) {
            prevI = 1.0f;
        }
        Log.d(TAG, " prevI1: " + prevI);

        for (float i = minZoomLevel; i <= maxZoomLevel; i += 1.0f) {
            float x = (i - minZoomLevel) * tickSpacing + offsetX;

            if (x >= 0f && x <= width) {
                // Vẽ các tick marks chính
                canvas.drawLine(x, centerY - 15, x, centerY + 15, linePaint);

                canvas.save();
                canvas.rotate(degree, x, centerY + 50);
                if (i != 0.5f) {
                    canvas.drawText(String.format("%.0fx", i), x, centerY + 50, textPaint);
                } else {
                    canvas.drawText(String.format("%.1fx", i), x, centerY + 50, textPaint);
                }
                Log.d(TAG, " minZoomLevel: " + minZoomLevel);
                Log.d(TAG, " prevI2: " + prevI);

                canvas.restore();
                // Vẽ các tick marks phụ (4 đường kẻ ngắn)
                if (i >= minZoomLevel && i < maxZoomLevel) {
                    for (int j = 1; j <= subMarks * (i - prevI); j++) {
                        float subX = x + j * (tickSpacing / (subMarks + 1));
                        System.out.println("minZoomLevel::" + subX);
                        canvas.drawLine(subX, centerY - 5, subX, centerY + 15, linePaint);
                    }
                }
            }

            if (i == 0.5f) {
                i = 0;
            }

            prevI = i;

        }

        drawVerticalArrow(canvas, centerX, centerY);

        // Vẽ giá trị zoom hiện tại bên dưới mũi tên
//        drawZoomValue(canvas, centerX, arrowY + 70);
    }

    private void drawVerticalArrow(Canvas canvas, int x, int y) {
        canvas.drawLine(x, y + 15, x, y - 50, linePaintVerticalArrow);

        float currentZoom = minZoomLevel + (x - offsetX) / tickSpacing;

        currentZoom = Math.max(minZoomLevel, Math.min(maxZoomLevel, currentZoom));

        canvas.save();
        canvas.rotate(degree, x, y - 50 - 5);
        canvas.drawText(String.format("%.1fx", currentZoom), x, y - 50 - 5, textPaintVerticalArrow);

        canvas.restore();

        // Gọi listener khi zoom level thay đổi
        if (zoomLevelChangeListener != null) {
            zoomLevelChangeListener.onZoomLevelChanged(currentZoom);
        }
    }

    private void drawZoomValue(Canvas canvas, int x, int y) {
        // Vẽ giá trị zoom hiện tại (giá trị ở giữa thanh thước)
        float currentZoom = minZoomLevel + (x - offsetX) / tickSpacing;

        // Đảm bảo giá trị zoom nằm trong phạm vi minZoomLevel đến maxZoomLevel
        currentZoom = Math.max(minZoomLevel, Math.min(maxZoomLevel, currentZoom));
        canvas.drawText(String.format("%.1fx", currentZoom), x, y, textPaint);

        // Gọi listener khi zoom level thay đổi
        if (zoomLevelChangeListener != null) {
            zoomLevelChangeListener.onZoomLevelChanged(currentZoom);
        }
    }

    // Hàm lắng nghe sự kiện chạm vuốt vào thanh thước
    private boolean isDragging = false;
    private float lastTouchX; // Biến để lưu trữ vị trí chạm cuối cùng

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!overScroller.isFinished()) {
                    overScroller.abortAnimation();
                }

                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(event);

                // Lưu trữ vị trí chạm ban đầu
                lastTouchX = event.getX();
                isDragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);

                float dx = event.getX() - lastTouchX; // Tính toán sự thay đổi so với lần chạm cuối

                if (!isDragging) {
                    // Kiểm tra nếu sự thay đổi trong vị trí chạm đủ lớn để coi là vuốt
                    if (Math.abs(dx) > 3) {
                        isDragging = true;
                    }
                }

                if (isDragging) {
                    offsetX += dx;

                    // Điều chỉnh lại giới hạn offsetX
                    int width = getWidth();
                    int totalRange = (int) ((maxZoomLevel - minZoomLevel) * tickSpacing);
                    offsetX = Math.max(-totalRange + width / 2, Math.min(width / 2, offsetX));

                    invalidate();
                }

                lastTouchX = event.getX(); // Cập nhật vị trí chạm cuối
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);
                    float flingVelocityX = velocityTracker.getXVelocity();

                    overScroller.fling(
                            (int) offsetX, 0,
                            (int) -flingVelocityX, 0,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            0, 0
                    );
                    postInvalidateOnAnimation();
                }

                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                isDragging = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    // Phương thức thiết lập mức zoom tối đa và tối thiểu
    public void setMaxZoomLevel(float maxZoomLevel) {
        this.maxZoomLevel = maxZoomLevel;
        invalidate();
    }

    public void setMinZoomLevel(float minZoomLevel) {
        this.minZoomLevel = minZoomLevel;
        invalidate();
    }

    public void setRotateOrientation(float degree) {
        this.degree = degree;
        invalidate();
    }

    // Phương thức để cập nhật mức zoom hiện tại và điều chỉnh vị trí thanh thước
    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = Math.max(minZoomLevel, Math.min(maxZoomLevel, zoomLevel));

        // Tính toán lại offsetX dựa trên mức zoom mới
//        int width = getWidth();

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int width = displayMetrics.widthPixels;

        Log.d(TAG, "setZoomLevel zoomLevel: " + String.valueOf(zoomLevel));
        Log.d(TAG, "setZoomLevel width: " + String.valueOf(getWidth()));
//        Log.d(TAG,"setZoomLevel screenWidth: " + String.valueOf(screenWidth));

        offsetX = width / 2 - (zoomLevel - minZoomLevel) * tickSpacing;


        Log.d(TAG, "setZoomLevel offsetX: " + String.valueOf(offsetX));

        // Giới hạn offsetX trong khoảng cho phép
        int totalRange = (int) ((maxZoomLevel - minZoomLevel) * tickSpacing);
        offsetX = Math.max(-totalRange + width / 2, Math.min(width / 2, offsetX));
        Log.d(TAG, "setZoomLevel offsetX after: " + String.valueOf(offsetX));

        invalidate(); // Vẽ lại thước với vị trí mới
    }

    public float getMaxZoomLevel() {
        return maxZoomLevel;
    }
}

