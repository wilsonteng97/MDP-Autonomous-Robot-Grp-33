package com.example.mdp;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TiltSensor {
    SensorManager sensorManager;
    Sensor sensor;
    GameView map;

    public TiltSensor (SensorManager sm, Sensor sensor, GameView map) {
        this.sensorManager = sm;
        this.sensor = sensor;
        this.map = map;
    }

    public void registerListener () {
        sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener () {
        sensorManager.unregisterListener(gyroListener);
    }

    public SensorEventListener gyroListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        public void onSensorChanged(SensorEvent event) {
            final int LEFT = 5;
            final int UP = 3;
            final int RIGHT = 1;
            final int DOWN = 7;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            int direction = UP;

            if (x > 4) direction = LEFT;
            else if (x < -4) direction = RIGHT;
            else if (y > 4) direction = DOWN;
            else if (y < -4) direction = UP;

            int transformedDir;
            switch (direction) {
                case UP:
                    transformedDir = 90;
                    break;
                case DOWN:
                    transformedDir = 270;
                    break;
                case LEFT:
                    transformedDir = 180;
                    break;
                case RIGHT:
                    transformedDir = 0;
                    break;
                default:
                    transformedDir = 90;
                    break;
            }
            map.moveRobot(transformedDir, true);
            Log.i("Hello", Float.toString(x)+" "+Float.toString(y)+" "+Float.toString(z));
        }
    };
}