package com.jbsw.GoogleDrive;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

public class BackupRestoreBase
{
    protected static final String m_sBackupFolder = "MiTravels";

    protected GoogleSignInAccount m_Signin;
    protected Context m_Context;
    protected DriveServiceHelper m_DSO;
    protected String m_sFolderId;

    protected BackupRestoreBase(Context context, GoogleSignInAccount Signin)
    {
        m_Context = context;
        m_Signin = Signin;
        Initialise();
    }

    protected void Initialise()
    {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(m_Context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(m_Signin.getAccount());
        Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),  new GsonFactory(), credential)
                .setApplicationName("MiTravels").build();

        m_DSO = new DriveServiceHelper(googleDriveService);
    }


}
