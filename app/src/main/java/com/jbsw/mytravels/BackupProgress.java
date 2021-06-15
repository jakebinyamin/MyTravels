package com.jbsw.mytravels;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jbsw.GoogleDrive.BackupData;

public class BackupProgress extends BaseBackupRestoreProgress implements BackupData.BackupCallBack
{
    private static final String TAG = "TAGBKPBackupProgress";
    private TextView m_Status;
    private ProgressBar m_Progress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_progress);

        Log.d(TAG, "in OnCreate");
        m_Status = (TextView) findViewById(R.id.id_backup_status);
        m_Progress = (ProgressBar) findViewById((R.id.id_progressBar));

        m_Status.setText("Beginning Backup");

        SetupScreenSize();
        Log.d(TAG, "About to signin");
        SigninAndContinue();
    }


   private void SetupScreenSize()
    {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int nWidth = dm.widthPixels;
        int nHeight = dm.heightPixels;

        getWindow().setLayout((int)(nWidth*.6), (int) (nHeight*.2) );

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
//        params.y = 300;
//        params.x = -100;
        getWindow().setAttributes(params);
    }

    @Override
    protected void Start()
    {
        Log.d(TAG,"In Start() + Google Account: " + m_GoogleAccount);
        BackupData BD = new BackupData(this, m_GoogleAccount);
        BD.SetCallBack(this);
        Log.d(TAG,"Callback set..");
        Thread ThrdBkp = new Thread(BD);
        ThrdBkp.start();
        Log.d(TAG, "Thread started..");
    }

    @Override
    public void SetMessage(int id)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Setting up Msg id: " + id);
                m_Status.setText(id);
            }
        });
    }

    @Override
    public void setBackupCount(int nCnt)
    {
        m_nCntOfFilesToBkp = nCnt;
        m_CntDone = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Files to backup: " + nCnt);
                m_Progress.setMax(nCnt);
            }
        });
    }

    @Override
    public void IncrementProgress()
    {
        if (m_CntDone <= m_nCntOfFilesToBkp)
            m_CntDone++;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Increment: " + m_CntDone);
                m_Progress.setProgress(m_CntDone);
            }
        });
    }

    @Override
    public void Complete()
    {
        finish();
    }
}