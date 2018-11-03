package com.vitaviva.commongallery.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;


public class ScreenOrientationHelper implements SensorEventListener {
    private Activity mActivity;
    private int mOriginOrientation;
    private Boolean mPortraitOrLandscape;
    private SensorManager mSensorManager;
    private Sensor[] mSensors;
    private float[] mAccelerometerValues = new float[3];
    private float[] mMagneticFieldValues = new float[3];

    public ScreenOrientationHelper(Activity activity) {
        mActivity = activity;
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
    }

    public void enableSensorOrientation() {
        if (mSensors == null) {
            mOriginOrientation = mActivity.getRequestedOrientation();

            mSensors = new Sensor[2];
            mSensors[0] = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensors[1] = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            mSensorManager.registerListener(this, mSensors[0], SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensors[1], SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void disableSensorOrientation(boolean reset) {
        if (mSensors != null) {
            mSensorManager.unregisterListener(this, mSensors[0]);
            mSensorManager.unregisterListener(this, mSensors[1]);
            mSensors = null;

            if (reset == true) {
                mActivity.setRequestedOrientation(mOriginOrientation);
            }
        }
    }

    public void disableSensorOrientation() {
        disableSensorOrientation(true);
    }

    /**
     * 是否开启自动旋转
     * @return
     */
    public static boolean isRotationOn(Context context) {
        int status = 0;

        try {
            status = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 设置status的值改变屏幕旋转设置
        if (status == 0) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isRotationOn(mActivity)) {
            return;
        }
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagneticFieldValues = event.values;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerValues = event.values;
                break;
            default:
                break;
        }

        calculateOrientation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticFieldValues);
        SensorManager.getOrientation(R, values);

        if (mSensors != null) {
            if (mSensors[1] == null)
                calculateByAccelerometer(mAccelerometerValues);
            else
                calculateByOrientation(values);
        }
    }

    private void calculateByOrientation(float[] values) {
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        int orientation = mActivity.getRequestedOrientation();

        if ((-10.0f < values[1] && values[1] <= 10f) && values[2] < -40f) {// 向左
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (40.0f < values[1] && values[1] < 90.0f) { // 向下
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (-90.0f < values[1] && values[1] < -40.0f) { // 向上
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if ((-10.0f < values[1] && values[1] <= 10f) && values[2] > 40f) { // 向右
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;
        }
    }

    private void calculateByAccelerometer(float[] values) {
        int orientation = mActivity.getRequestedOrientation();

        if ((-2f < values[1] && values[1] <= 2f) && values[0] < 0) {// 向左
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (4f < values[1] && values[1] < 10f) { // 向下
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (-10f < values[1] && values[1] < -4f) { // 向上
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if ((-2f < values[1] && values[1] <= 2f) && values[0] > 0) { // 向右
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;
        }
    }
}
