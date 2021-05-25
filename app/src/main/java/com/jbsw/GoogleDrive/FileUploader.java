package com.jbsw.GoogleDrive;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUploader implements MediaHttpUploaderProgressListener
{
    private static final String TAG = "TAGBKPFileUploader";
    private DriveServiceHelper m_DSO;
    private List<String> m_FileListToUpload;
    private String m_sFolderId;
    private int m_nCurrFile;

    public FileUploader(DriveServiceHelper dso)
    {
        m_DSO = dso;
        m_FileListToUpload = new ArrayList<String>();
    }

    public void ClearList()
    {
        m_FileListToUpload.clear();
    }

    public void SetFolerId(String sId)
    {
        m_sFolderId = sId;
    }

    public void AddFileToList(String sFile)
    {
        m_FileListToUpload.add(sFile);
    }

    public void StartUpload()
    {
        m_nCurrFile = 0;
        UploadNextFile();
    }


    void UploadNextFile()
    {
        if (m_nCurrFile >= m_FileListToUpload.size()) {
            Log.d(TAG, "All files backed up.. " + m_nCurrFile);
            return;
        }

        String sFile = m_FileListToUpload.get(m_nCurrFile);
        m_nCurrFile++;

        //
        // Create the file
        Log.d(TAG, "Backing up: " + sFile);
        m_DSO.UploadFile(sFile, m_sFolderId, this);
    }

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException
    {
        if (uploader == null)
            return;

        if (uploader.getUploadState() == MediaHttpUploader.UploadState.MEDIA_COMPLETE) {
            Log.d(TAG, "Backup file complete");
            UploadNextFile();
        }
    }
}
