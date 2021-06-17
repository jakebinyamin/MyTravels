package com.jbsw.mytravels;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jbsw.GoogleDrive.BackupData;

public class BackupProgress extends BaseBackupRestoreProgress
{
    public BackupProgress()
    {
        m_Mode = Mode.MODE_BACKUP;
    }


    @Override
    protected void Start()
    {
        BackupData BD = new BackupData(this, m_GoogleAccount);
        BD.SetCallBack(this);
        Thread ThrdBkp = new Thread(BD);
        ThrdBkp.start();
    }

}