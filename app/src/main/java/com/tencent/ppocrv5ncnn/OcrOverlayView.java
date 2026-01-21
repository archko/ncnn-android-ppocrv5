package com.tencent.ppocrv5ncnn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class OcrOverlayView extends View {

    private List<BasePolygonResultModel> mResultModelList;
    private Paint paint;
    private Paint textPaint;
    private Path path;
    private int imgWidth;
    private int imgHeight;
    private boolean drawText = false;

    public OcrOverlayView(Context context) {
        super(context);
        init();
    }

    public OcrOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OcrOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(36);
        textPaint.setAntiAlias(true);

        path = new Path();
    }

    public void setPolygonListInfo(List<BasePolygonResultModel> modelList, int width, int height) {
        mResultModelList = modelList;
        imgWidth = width;
        imgHeight = height;
        postInvalidate();
    }

    public void clear() {
        mResultModelList = null;
        postInvalidate();
    }

    public void setDrawText(boolean drawText) {
        this.drawText = drawText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mResultModelList == null || mResultModelList.isEmpty() || imgWidth == 0 || imgHeight == 0) {
            return;
        }

        float viewWidth = getMeasuredWidth();
        float viewHeight = getMeasuredHeight();
        float bitmapWidth = imgWidth;
        float bitmapHeight = imgHeight;

        float scaleX = viewWidth / bitmapWidth;
        float scaleY = viewHeight / bitmapHeight;
        float scale = Math.min(scaleX, scaleY);
        float offsetX = (viewWidth - bitmapWidth * scale) / 2;
        float offsetY = (viewHeight - bitmapHeight * scale) / 2;

        for (BasePolygonResultModel model : mResultModelList) {
            // Draw rectangle border
            Rect rect = model.getRect(1.0f, new Point(0, 0));
            Rect scaledRect = new Rect(
                (int)(offsetX + rect.left * scale),
                (int)(offsetY + rect.top * scale),
                (int)(offsetX + rect.right * scale),
                (int)(offsetY + rect.bottom * scale)
            );

            path.reset();
            path.moveTo(scaledRect.left, scaledRect.top);
            path.lineTo(scaledRect.right, scaledRect.top);
            path.lineTo(scaledRect.right, scaledRect.bottom);
            path.lineTo(scaledRect.left, scaledRect.bottom);
            path.close();
            canvas.drawPath(path, paint);

            // Draw text label
            if (drawText) {
                String text = model.getName();
                if (text != null && !text.isEmpty()) {
                    canvas.drawText(text, scaledRect.left + 5, scaledRect.bottom - 5, textPaint);
                }
            }
        }
    }
}
