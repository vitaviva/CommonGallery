package com.vitaviva.commongallery.widget;

import android.view.animation.Interpolator;


public class CubicBezierInterpolator implements Interpolator {
    float mControlPoint1x;
    float mControlPoint1y;
    float mControlPoint2x;
    float mControlPoint2y;

    public CubicBezierInterpolator() {
        this(0.2f, -0.05f, 0.0f, 1.05f);
    }

    public CubicBezierInterpolator(float cx1, float cy1, float cx2, float cy2) {
        mControlPoint1x = 0.0f;
        mControlPoint1y = 0.0f;
        mControlPoint2x = 0.0f;
        mControlPoint2y = 0.0f;
        mControlPoint1x = cx1;
        mControlPoint1y = cy1;
        mControlPoint2x = cx2;
        mControlPoint2y = cy2;
    }

    @Override
    public float getInterpolation(float input) {
        return getCubicBezierY(2.5E-4f * ((float) binarySearch(input)));
    }

    private float getCubicBezierX(float t) {
        return ((((((1.0f - t) * 3.0f) * (1.0f - t)) * t) * mControlPoint1x) + (((((1.0f - t) * 3.0f) * t) * t) * mControlPoint2x)) + ((t * t) * t);
    }

    protected float getCubicBezierY(float t) {
        return ((((((1.0f - t) * 3.0f) * (1.0f - t)) * t) * mControlPoint1y) + (((((1.0f - t) * 3.0f) * t) * t) * mControlPoint2y)) + ((t * t) * t);
    }

    long binarySearch(float key) {
        long low = 0;
        long high = 4000;
        while (low <= high) {
            long j = (low + high) >>> 1;
            float approximation = getCubicBezierX(2.5E-4f * ((float) j));
            if (approximation < key) {
                low = j + 1;
            } else if (approximation <= key) {
                return j;
            } else {
                high = j - 1;
            }
        }
        return low;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("CubicBezierInterpolator");
        sb.append("  mControlPoint1x = " + mControlPoint1x);
        sb.append(", mControlPoint1y = " + mControlPoint1y);
        sb.append(", mControlPoint2x = " + mControlPoint2x);
        sb.append(", mControlPoint2y = " + mControlPoint2y);
        return sb.toString();
    }
}
