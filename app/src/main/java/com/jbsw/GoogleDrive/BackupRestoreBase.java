package com.jbsw.GoogleDrive;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class BackupRestoreBase
{
    private static final String TAG = "TAGBKPBackupData";
    protected static final String m_sBackupFolder = "MiTravels";

    protected GoogleSignInAccount m_Signin;
    protected Context m_Context;
    protected DriveServiceHelper m_DSO;
    protected String m_sFolderId;

    protected BackupRestoreBase(Context context, GoogleSignInAccount Signin)
    {
        Log.d(TAG, "in BackupRestoreBase constructor, Signin: " + Signin);
        m_Context = context;
        m_Signin = Signin;
        Initialise();
        Log.d(TAG, "in BackupRestoreBase constructor - after Initialise()");
    }

    protected void Initialise()
    {
        Drive googleDriveService = null;

        try {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(m_Context, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(m_Signin.getAccount());
            googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName("MiTravels").build();
        }
        catch(Exception e)
        {
            Log.d(TAG, "Problem setting up googleDriveService: " + e.getMessage());
            m_DSO = null;
            return;
        }

        m_DSO = new DriveServiceHelper(googleDriveService);
    }


}
