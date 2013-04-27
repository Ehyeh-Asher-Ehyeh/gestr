package net.nexcius.accelproxy;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AccelService extends Service implements SensorEventListener {
    private AccelTask remoteClient;
    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mOrientation = null;
    
    public float acceleration = 0.0f;
    public float orientation = 0.0f;
    
    public boolean changed = false;
    
    public float accelX = 0.0f;
    public float accelY = 0.0f;
    public float accelZ = 0.0f;
    
    private float standardAcceleration = 0.0f;
    private float standardOrientation = 0.0f;
    
    private int calibrationCountA = 0;
    private int calibrationCountO = 0;
    private int calibrationDelta = 10;
    private boolean doneCalibrating = false;
    
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Notification noti = new NotificationCompat.Builder(this).setContentTitle("Transferring sensor data").setSmallIcon(R.drawable.ic_launcher).build();
        startForeground((int) System.currentTimeMillis(), noti);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE); 
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WL_TAG");
        wakeLock.acquire();

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        //mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_FASTEST);


        Bundle extras = intent.getExtras();
        String ip = extras.getString("SERVER_IP");

        remoteClient = new AccelTask(ip, this);
        Log.d("NEX_SERVICE_TAG", "Starting Service.");
        remoteClient.execute();

        return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        stopForeground(true);
        mSensorManager.unregisterListener(this);
        remoteClient.cancel(true);
        Log.d("NEX_SERVICE_TAG", "Stopped Service");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == mAccelerometer) {
            accelX = event.values[0];
            accelY = event.values[1];
            accelZ = event.values[2];
            changed = true;
        } else if(event.sensor == mOrientation) {
            orientation = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
