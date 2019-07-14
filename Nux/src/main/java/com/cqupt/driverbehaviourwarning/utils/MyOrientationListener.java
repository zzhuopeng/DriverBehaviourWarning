package com.cqupt.driverbehaviourwarning.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Direction sensor monitoring
 */
public class MyOrientationListener implements SensorEventListener {

    private static final String TAG = "MyOrientationListener";

    private Context context;
    private SensorManager sensorManager;
    private Sensor sensor;

    //The orientation sensor has three coordinates and now only focuses on X
    private float lastX;

    private OnOrientationListener onOrientationListener;

    public MyOrientationListener(Context context) {
        this.context = context;
    }

    public float getlastX() {
        return lastX;
    }

    //Start detection
    public void start() {
        //Get Sensor Manager
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Get direction sensor
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        //register
        if (sensor != null) {//SensorManager.SENSOR_DELAY_UI
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }

    }

    //Stop detection
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    /**
     * called when sensor accuracy changes
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Accepts the type of direction sensor
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            //Here we can get the data and then process it as needed
            float x = event.values[SensorManager.DATA_X];
            //To prevent frequent updates
            if (Math.abs(x - lastX) > 1.0) {
                onOrientationListener.onOrientationChanged(x);
            }
//            Log.i(TAG, String.valueOf((int) x));
            lastX = x;
        }
    }

    public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
        this.onOrientationListener = onOrientationListener;
    }

    //Callback method
    public interface OnOrientationListener {
        void onOrientationChanged(float x);
    }

}
