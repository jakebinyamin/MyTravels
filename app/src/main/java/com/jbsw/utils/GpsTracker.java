package com.jbsw.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.jbsw.data.DBManager;
import com.jbsw.data.GpsDataTable;
import com.jbsw.data.TravelMasterTable;
import com.jbsw.mytravels.MainActivity;
import com.jbsw.mytravels.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

// TODO deal with duplicate locations coming through
public class GpsTracker extends Service
{
    private static final String TAG = "TAGGpsTracker";
    public static final String CHANNEL_ID = "GpsTrackerChannel";
    private LocationManager m_LocationManager = null;

    public static final long MIN_DISTANCE = 300; // 300 meters
    private static final long MIN_TIME = 1000 * 5; // 1000 * 60 * 1; // 1 minute
    private MyLocationListener m_LocatiionListener;
    NotificationCompat.Builder m_Notification;
    NotificationManager m_NotificationManager = null;
    long m_Distance;
    private boolean m_bIsMonitoring = false;
    static public GpsTracker m_This;

    boolean m_isGPSEnabled = false;

    Context m_Context = null;

    static public GpsTracker GetTracker()
    {
        if (m_This == null)
            Log.e(TAG, "*** Tracker NOT CREATED ***");
        return m_This;
    }
    public GpsTracker()
    {
        m_This = this;
        Log.d(TAG, "Tracker constructor");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");

        m_Context = getApplicationContext();
        if (m_Context == null) {
            Log.e(TAG," m_Context is null");
            return;
        }

        initializeLocationManager();
        m_LocatiionListener = new MyLocationListener(LocationManager.GPS_PROVIDER); //PASSIVE_PROVIDER);

        /*
        try {
            Prefs prefs = new Prefs(this);
            m_Distance = prefs.GetGpsTrackerData();
//            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, m_LocatiionListener); // dbg
            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,m_Distance, m_LocatiionListener);
//            m_LocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,MIN_TIME,MIN_DISTANCE, m_LocatiionListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        */

//        Location loc = GetLocation();
//        m_LocatiionListener.BroadcastLastLocation(loc);
        Log.d(TAG, "onCreate completed successfully..");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        m_Notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
//                .setContentText(getResources().getString(R.string.notificationmessage))
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setNumber(0)
                .setContentIntent(pendingIntent);

        //
        // If service is starting up without Main activity, then we need to create this here.
        if (DBManager.Get() == null) {
            Log.d(TAG, "Creating new DBManager.");
            DBManager.Create(getApplicationContext());
        }

        CheckToStartGPSMonitor();
//        startForeground(1, m_Notification.build());

        Log.d(TAG, "onStartCommand finished after CheckToStartGPSMonitor");
        return START_STICKY;
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_LOW);
        serviceChannel.setShowBadge(false);
        m_NotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE/*NotificationManager.class*/);
        m_NotificationManager.createNotificationChannel(serviceChannel);
    }

    public void CheckToStartGPSMonitor()
    {
        Log.d(TAG, "In CheckToStartGPSMonitor()");
        TravelMasterTable Master = new TravelMasterTable();

        //
        // If there is nothing to monitor then turn it off
        if (!Master.QueryAllInProgress()) {
            Log.d(TAG, "No Matching Master Records.. Nothing to monitor");
            if (m_bIsMonitoring) {
                if (m_LocationManager != null)
                    m_LocationManager.removeUpdates(m_LocatiionListener);
                stopForeground(true);
                m_bIsMonitoring = false;
                Log.d(TAG, "Nothing to monitor.. Was Monitoring GPS - Now turning off");
            }
            return;
        }

        //
        // If monitoring already then we are fine
        if (m_bIsMonitoring) {
            Log.d(TAG, "Need to monitor. Was Monitoring GPS - Nothing to do");
            return;
        }

        //
        // Start monitoring
        TravelMasterTable.DataRecord MasterRec;
        MasterRec = Master.GetNextRecord();
        if (MasterRec == null) {
            Log.e(TAG, "No MasterRec - Shouldnt get here");
            return; // Should never get here, just being safe
        }

        Log.d(TAG, "Need to monitor. Was *NOT* Monitoring GPS - Need to monitor");
        try {
            Prefs prefs = new Prefs(this);
            m_Distance = prefs.GetGpsTrackerData();
            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,m_Distance, m_LocatiionListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        Log.d(TAG, "Monitoring with Distance: " + m_Distance);
        String sText = String.format(getResources().getString(R.string.notificationmessage), MasterRec.Name);
        m_Notification.setContentText(sText);

        startForeground(1, m_Notification.build());
        m_bIsMonitoring = true;
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (m_LocationManager != null)
            m_LocationManager.removeUpdates(m_LocatiionListener);
    }

    private void initializeLocationManager()
    {
        if (m_LocationManager == null)
            m_LocationManager = (LocationManager) m_Context.getSystemService(Context.LOCATION_SERVICE);

        m_isGPSEnabled = m_LocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (m_LocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
            Log.e(TAG, "Passive provider not Enabled");
        if (m_isGPSEnabled)
            Log.d(TAG, "gps is enabled..");

    }

    @SuppressLint("MissingPermission")
    public Location GetLocation()
    {
        Location MyLocation = null;
        if (m_LocationManager != null) {
            MyLocation = m_LocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "GetLocation returned a location: " + MyLocation);
        }
        return MyLocation;
    }

    public void CheckForChangeInDistanceTracking()
    {
        Prefs prefs = new Prefs(this);
        long lNewDistance = prefs.GetGpsTrackerData();
        Log.d(TAG,"Before Requested change in Distance tracking: " + m_Distance + " NewDistance: " + lNewDistance);
        if (m_Distance == lNewDistance)
            return;

        m_Distance = lNewDistance;
        Log.d(TAG,"After Requested change in Distance tracking: " + m_Distance + " NewDistance: " + lNewDistance);
        m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,m_Distance, m_LocatiionListener);
    }

    //////////////////////////////////////////////////////////////////////////////
    // LocationListener..
    private class MyLocationListener implements android.location.LocationListener
    {
        Location m_LastLocation = null;
        String  m_sProvider;

        public MyLocationListener(String provider)
        {
            m_sProvider = provider;
            m_LastLocation = null;
            Log.e(TAG, "LocationListener " + provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);

            if (FilterLocation(location))
                return;

            BroadcastLastLocation(location);

            AddRecord();
            CheckForChangeInDistanceTracking();
        }

        private void BroadcastLastLocation(Location location)
        {
            if (location == null)
                return;

            if (m_LastLocation == null)
                m_LastLocation = new Location(m_sProvider);
            m_LastLocation.set(location);

            //
            // Broadcast the data
//            Intent intent = new Intent();
//            intent.setAction("com.jbw.MyTravels.LocData");
//            intent.putExtra("Longitude", m_LastLocation.getLongitude());
//            intent.putExtra("Latitude", m_LastLocation.getLatitude());
//            sendBroadcast(intent);
//            Log.d(TAG, "Broadcasting location: " + location);
        }

        private boolean FilterLocation(Location location)
        {
            if (m_LastLocation != null && m_LastLocation.distanceTo(location) < m_Distance) {
                Log.e(TAG, "Location filterd by Min Distance .. mLastLocation is: " + m_LastLocation);
                return true;
            }
            
            if (LocationIsOld(location))
                return true;

            float horizontalAccuracy = location.getAccuracy();
            if(horizontalAccuracy > 100)
            {
                Log.e(TAG, "Location filterd by Accuracy..");
                return false; // DEBUG true;
            }

            return false;
        }

        private boolean LocationIsOld(Location newLocation)
        {
            long locationAge;
            if(android.os.Build.VERSION.SDK_INT >= 17)
            {
                long currentTimeInMilli = (long)(SystemClock.elapsedRealtimeNanos() / 1000000);
                long locationTimeInMilli = (long)(newLocation.getElapsedRealtimeNanos() / 1000000);
                locationAge = currentTimeInMilli - locationTimeInMilli;
            }else{
                locationAge = System.currentTimeMillis() - newLocation.getTime();
            }

            if (locationAge > 10 * 1000) {
                Log.e(TAG, "Location Filtered by age..");
                return true;
            }

            return false;
        }

        private void AddRecord()
        {
            //
            // Query the master table
            TravelMasterTable Master = new TravelMasterTable();
            if (!Master.QueryAllInProgress()) {
                Log.e(TAG, "No master records found");
                return;
            }

            TravelMasterTable.DataRecord MasterRec;
            String sName = null;
            while ((MasterRec = Master.GetNextRecord()) != null)
            {
                Log.d(TAG, "Writing GPS Record. ");
                GpsDataTable TabGps = new GpsDataTable();
                TabGps.AddGPSRecord(MasterRec.Id, m_LastLocation);
                sName = MasterRec.Name;
            }

            if (sName != null)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String date = sdf.format(new Date());
                String sText = String.format(getResources().getString(R.string.lastgps), sName, date);
                Log.d(TAG, "Updating Notification with: " + sText);

                m_Notification.setContentText(sText);
                m_Notification.setNumber(0);

                if (m_NotificationManager != null) {
                    m_NotificationManager.notify(1, m_Notification.build());
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
    //////////////////////////////////////////////////////////////////////////////
}
