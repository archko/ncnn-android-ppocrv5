package com.tencent.ppocrv5ncnn;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class BasePolygonResultModel {
    private Rect rect;
    private String name;
    private float confidence;

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public List<Point> getBounds(float sizeRatio, Point originPt) {
        List<Point> points = new ArrayList<>();
        points.add(new Point((int)(originPt.x + rect.left * sizeRatio), (int)(originPt.y + rect.top * sizeRatio)));
        points.add(new Point((int)(originPt.x + rect.right * sizeRatio), (int)(originPt.y + rect.top * sizeRatio)));
        points.add(new Point((int)(originPt.x + rect.right * sizeRatio), (int)(originPt.y + rect.bottom * sizeRatio)));
        points.add(new Point((int)(originPt.x + rect.left * sizeRatio), (int)(originPt.y + rect.bottom * sizeRatio)));
        return points;
    }

    public boolean isMultiplePairs() {
        return false;
    }

    public boolean isDrawPoints() {
        return false;
    }

    public boolean isHasMask() {
        return false;
    }

    public boolean isRect() {
        return true;
    }

    public Rect getRect(float sizeRatio, Point originPt) {
        return new Rect((int)(originPt.x + rect.left * sizeRatio), (int)(originPt.y + rect.top * sizeRatio),
                (int)(originPt.x + rect.right * sizeRatio), (int)(originPt.y + rect.bottom * sizeRatio));
    }

    public String getName() {
        return name;
    }

    public float getConfidence() {
        return confidence;
    }

    public boolean isTextOverlay() {
        return false;
    }

    public boolean isSemanticMask() {
        return false;
    }

    public byte[] getMask() {
        return null;
    }

    public int getColorId() {
        return 0;
    }

    public boolean isHasGroupColor() {
        return false;
    }
}
