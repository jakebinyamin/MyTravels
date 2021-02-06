package com.jbsw.mytravels;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jbsw.utils.GpsTracker;
import com.jbsw.utils.Prefs;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashScreen extends Activity
{
    private static final String TAG = "TAGSplashScreen";
    private int waitInterval = 3000;
    static private final int  PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        View v = findViewById(R.id.splash);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        Log.d(TAG, "In onCreate...");
        String versionNumber = "";
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // extract the build number from the versionName
        if (info != null)
            versionNumber = info.versionName;

        TextView tv = (TextView) findViewById(R.id.version_title);
        String sVsn = String.format(getResources().getString(R.string.version_s), versionNumber);
        tv.setText(sVsn);

        CheckPermissions();
    }


    private boolean CheckPermissions()
    {
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                List<String> permissions = new ArrayList<>();
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                final String[] perms = permissions.toArray(new String[permissions.size()]);
                Log.d(TAG, "ABout to request Permission");
                ActivityCompat.requestPermissions(this, perms, PERMISSION_CODE);
                Log.d(TAG, "request Permission called");
                return false;
            }
            else
            {
                StartAppAfterWait();
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (requestCode != PERMISSION_CODE)
            return;

        Log.d(TAG, "In onRequestPermissionsResult - grantResults.length: " + grantResults.length);
        if (grantResults.length <= 0)
            return;

        // If request is cancelled, the result arrays are empty.
        boolean grantedAll = true;
        for (int i = 0; i < grantResults.length; i++) {
            Log.d(TAG, "permission " + permissions[i] + " granted? " + (grantResults[i] == PackageManager.PERMISSION_GRANTED));
            if ((grantResults[i] != PackageManager.PERMISSION_GRANTED))
                grantedAll = false;
        }

        StartAppAfterWait();

//        if (grantedAll) {
//            Log.d(TAG, "All Permissions Granted..");
//            StartAppAfterWait();
//        } else {
//            finish();
//        }
        return;
    }

    private void StartAppAfterWait()
    {
        Handler MyHandler = new Handler();
        StartInBackground Start = new StartInBackground();
        MyHandler.postDelayed(Start, waitInterval);
    }

    private class StartInBackground implements Runnable
    {
        @Override
        public void run()
        {
//            //
//            // Start the gps receiver
//            Intent MyIntent = new Intent(SplashScreen.this, GpsTracker.class);
//            SplashScreen.this.startService(MyIntent);

            //
            // Start the main app
//            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            Intent intent;
            Prefs prefs = new Prefs(SplashScreen.this);
            if (prefs.ShowIntro()) {
                intent = new Intent(SplashScreen.this, Intro.class);
            }
            else {
                intent = new Intent(SplashScreen.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }
}


