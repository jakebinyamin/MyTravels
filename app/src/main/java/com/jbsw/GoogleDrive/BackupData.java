package com.jbsw.GoogleDrive;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.jbsw.data.DBToJson;
import com.jbsw.data.PhotoLinkTable;

import java.util.Collections;

public class BackupData implements Runnable
{
    private static final String TAG = "TAGBKPBackupData";
    private static final String m_sBackupFolder = "MiTravels";

    private GoogleSignInAccount m_Signin;
    private Context m_Context;
    private DriveServiceHelper m_DSO;
    private String m_sFolderId;
    private FileUploader m_FileUPloader;

    public BackupData(Context context, GoogleSignInAccount Signin)
    {
        m_Context = context;
        m_Signin = Signin;
        Initialise();
    }

    private void Initialise()
    {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        m_Context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(m_Signin.getAccount());
        Drive googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),  new GsonFactory(), credential)
                .setApplicationName("MiTravels").build();

        m_DSO = new DriveServiceHelper(googleDriveService);
        m_FileUPloader = new FileUploader(m_DSO);
    }

    @Override
    public void run()
    {
        DBToJson Json = new DBToJson(m_Context);
        if (!Json.Export()) {
            Log.e(TAG, "Failed to Create JSON");
            //TODO add error message
            return;
        }

        //
        // Create the backup folder
        if (!CreateBackupFolder()) {
            Log.e(TAG, "Failed to Create Folder");
            //TODO add error message
            return;
        }

        Log.e(TAG, "Folder ID: " + m_sFolderId);

        m_FileUPloader.SetFolderId(m_sFolderId);
        m_FileUPloader.SetUploadFileList(Json.GetFilesToUpload());

        //
        // Gather files
//        CollectPhotos();
        m_FileUPloader.StartUpload();
        Log.d(TAG, "Exiting the Backup Thread..");

    }

    private boolean CreateBackupFolder()
    {
        //
        // Check if there is a backup folder already there
        String sFolderId = m_DSO.FindFolder(m_sBackupFolder);
        if (sFolderId != null) {
            Log.d(TAG, "Delete folder: " + sFolderId);
            boolean bRet = m_DSO.DeleteFie(sFolderId);
            Log.d(TAG, "Folder deleted: " + bRet);
        }

        // Recreate the folder
        m_sFolderId = m_DSO.AsyncCreateFolder(m_sBackupFolder);
        return (m_sFolderId != "");
    }
}
