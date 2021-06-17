package com.jbsw.mytravels;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;
import com.jbsw.GoogleDrive.BackupCallBack;
import com.jbsw.GoogleDrive.BackupData;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseBackupRestoreProgress extends AppCompatActivity implements View.OnClickListener, BackupCallBack
{
    private static final String TAG = "TAGBKPBaseProgress";

    protected GoogleSignInAccount m_GoogleAccount;
    protected static final int SIGN_IN_BR = 1;
    private static final String WEB_CLIENT_ID = "821524552405-s5aukovdnvl7vpoh10eqs541tfms41u4.apps.googleusercontent.com";
    protected int m_nCntOfFilesToBkp = 0;
    protected int m_CntDone;

    private TextView m_Status;
    private ProgressBar m_Progress;
    protected Button m_BtnClose;

    protected enum Mode { MODE_BACKUP, MODE_RESTORE }
    protected Mode m_Mode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_progress);

        m_BtnClose = findViewById(R.id.id_close);
        m_BtnClose.setOnClickListener(this);
        m_BtnClose.setVisibility(View.GONE);

        m_Status = (TextView) findViewById(R.id.id_backup_status);
        m_Progress = (ProgressBar) findViewById((R.id.id_progressBar));

        setFinishOnTouchOutside(false);

        TextView Title = findViewById(R.id.id_title);
        if (m_Mode == Mode.MODE_BACKUP)
            Title.setText(R.string.backup_progress);
        if (m_Mode == Mode.MODE_RESTORE)
            Title.setText(R.string.restore_progress);

        SetupScreenSize();
        SigninAndContinue();
    }

    private void SetupScreenSize()
    {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int nWidth = dm.widthPixels;
        int nHeight = dm.heightPixels;

        getWindow().setLayout((int)(nWidth*.6), (int) (nHeight*.23) );

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnClose)
            finish();
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
        Log.d(TAG,"In Complete About to set button to visible...");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (m_Mode == Mode.MODE_BACKUP)
                    SetMessage(R.string.bkp_complete);
                if (m_Mode == Mode.MODE_RESTORE)
                    SetMessage(R.string.restore_complete);
                m_BtnClose.setVisibility(View.VISIBLE);
            }
        });
        Log.d(TAG,"In Complete button SET to visible...");
    }


    protected void SigninAndContinue()
    {
        m_GoogleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (m_GoogleAccount != null) {
            Log.d(TAG, "Signing Previously Successful!! - Account:  " + m_GoogleAccount.getEmail());
            Start();
            return;
        }

        Log.d(TAG, "Need to signin");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(WEB_CLIENT_ID)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);

        startActivityForResult(client.getSignInIntent(), SIGN_IN_BR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData)
    {
        if (requestCode != SIGN_IN_BR)
            return;
        Log.d(TAG, "in onActivityResult for REQUEST_CODE_SIGN_IN, ResultCode: " + resultCode + ", resultData: " + resultData );
        if (/*resultCode == Activity.RESULT_OK && */resultData != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(resultData);
            try {
                m_GoogleAccount = task.getResult(ApiException.class);
                Log.d(TAG, "Signing Successful!! - Account: " + m_GoogleAccount.getEmail());
                Start();
            } catch (ApiException e) {
                Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    protected abstract void Start();
}
