package com.jbsw.mytravels;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseBackupRestoreProgress extends AppCompatActivity
{
    private static final String TAG = "TAGBKPBaseProgress";

    protected GoogleSignInAccount m_GoogleAccount;
    protected static final int SIGN_IN_BR = 1;
    private static final String WEB_CLIENT_ID = "821524552405-s5aukovdnvl7vpoh10eqs541tfms41u4.apps.googleusercontent.com";
    protected int m_nCntOfFilesToBkp = 0;
    protected int m_CntDone;

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
