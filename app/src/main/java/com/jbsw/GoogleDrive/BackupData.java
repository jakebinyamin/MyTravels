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
import com.jbsw.mytravels.R;

import java.util.Collections;

public class BackupData extends BackupRestoreBase implements Runnable
{
    private static final String TAG = "TAGBKPBackupData";
    private FileUploader m_FileUPloader;

    public BackupData(Context context, GoogleSignInAccount Signin)
    {
        super(context, Signin);
        m_CallBack = null;
        m_FileUPloader = new FileUploader(m_DSO);
    }

    @Override
    public void run()
    {
        Log.d(TAG, "In thread..");
        DBToJson Json = new DBToJson(m_Context);
        SetString(R.string.bkp_prepare);
        if (!Json.Export()) {
            Log.e(TAG, "Failed to Create JSON");
            Complete();
            return;
        }

        //
        // Create the backup folder
        if (!CreateBackupFolder()) {
            Log.e(TAG, "Failed to Create Folder");
            Complete();
            return;
        }

        Log.e(TAG, "Folder ID: " + m_sFolderId);

        m_FileUPloader.SetCallback(m_CallBack);
        m_FileUPloader.SetFolderId(m_sFolderId);
        m_FileUPloader.SetUploadFileList(Json.GetFilesToUpload());

        //
        // Gather files
        SetString(R.string.bkp_upload);
        m_FileUPloader.StartUpload();
        Complete();
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
