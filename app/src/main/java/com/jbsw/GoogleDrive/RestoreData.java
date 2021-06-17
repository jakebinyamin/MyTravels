package com.jbsw.GoogleDrive;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jbsw.data.JsonToDB;
import com.jbsw.mytravels.R;

import java.io.File;
import java.util.ArrayList;

import androidx.core.util.Pair;

public class RestoreData extends BackupRestoreBase implements Runnable
{
    private static final String TAG = "TAGBKPRestoreData";
    private ArrayList<Pair<String,String>> m_FilesToDownload;

    public RestoreData(Context context, GoogleSignInAccount Signin)
    {
        super(context, Signin);
        Initialise();
    }

    @Override
    public void run()
    {
        Log.d(TAG, "Starting Restore Thread");
        //
        // Get the backup folder id
        SetString(R.string.restore_prepare);
        String sFolderId = m_DSO.FindFolder(m_sBackupFolder);
        if (sFolderId == null) {
            Log.d(TAG, "No Backup found");
            return;
        }

        //
        // Download the files
        m_FilesToDownload = m_DSO.FindFilesInFolder(sFolderId);

        int nCnt = m_FilesToDownload.size();
        if (m_CallBack != null) {
            m_CallBack.setBackupCount(nCnt);
            SetString(R.string.restore_download);
        }

        for (int i = 0; i < nCnt; i++) {
            Log.d(TAG, "File Item: " + m_FilesToDownload.get(i).first + ", " + m_FilesToDownload.get(i).second);
            File fOutFile = new File(m_Context.getFilesDir(), m_FilesToDownload.get(i).first);
            String sOutFile = fOutFile.getAbsolutePath();
            m_DSO.DownloadFile(sOutFile, m_FilesToDownload.get(i).second);
            if (m_CallBack != null)
                m_CallBack.IncrementProgress();
        }

        //
        // Rebuild the database
        SetString(R.string.restore_import);
        File jsonFile = new File(m_Context.getFilesDir(), "MTDBExport.json");
        String sJsonFile = jsonFile.getAbsolutePath();
        JsonToDB json = new JsonToDB(m_Context);
        json.Import(sJsonFile);

        Complete();
    }
}
