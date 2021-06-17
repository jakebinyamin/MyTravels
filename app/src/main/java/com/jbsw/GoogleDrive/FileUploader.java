package com.jbsw.GoogleDrive;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileUploader implements MediaHttpUploaderProgressListener
{
    private static final String TAG = "TAGBKPFileUploader";
    private DriveServiceHelper m_DSO;
    private HashMap<String, String> m_UploadFileList;
    private Iterator<Map.Entry<String, String>> m_FileListIterator;
    private String m_sFolderId;
    private int m_nCurrFile;
    private BackupCallBack m_Callback = null;

    public FileUploader(DriveServiceHelper dso)
    {
        m_DSO = dso;
    }

    public void SetFolderId(String sId)
    {
        m_sFolderId = sId;
    }
    public void SetCallback(BackupCallBack cb)
    {
        m_Callback = cb;
    }

    public void SetUploadFileList(HashMap<String, String>UploadFileList)
    {
        m_UploadFileList = UploadFileList;

        if (m_Callback != null)
            m_Callback.setBackupCount(m_UploadFileList.size());

        for (String i : m_UploadFileList.keySet()) {
            Log.e(TAG, "KEY: " + i);
        }
        for (String i : m_UploadFileList.values()) {
            Log.e(TAG, "VALUE: " + i);
        }

        m_FileListIterator = m_UploadFileList.entrySet().iterator();

    }

    public void StartUpload()
    {
        m_nCurrFile = 0;
        UploadNextFile();
    }


    void UploadNextFile()
    {
        if (!m_FileListIterator.hasNext()) {
            Log.d(TAG, "All files backed up.. " + m_nCurrFile);
            return;
        }

        if (m_Callback != null)
            m_Callback.IncrementProgress();

        //
        // Get source file and destination file
        Map.Entry<String, String> entry = m_FileListIterator.next();
        String sSourceFile = entry.getKey();
        String sDestnFile = entry.getValue();

        //
        // Create the file
        Log.d(TAG, "Backing up Source: " + sSourceFile + " Destn: " + sDestnFile);
        if (!m_DSO.UploadFile(sSourceFile, sDestnFile, m_sFolderId, this)) {
            Log.e(TAG, "Backup file failed..");
            UploadNextFile();
        }
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
        if (uploader.getUploadState() == MediaHttpUploader.UploadState.NOT_STARTED) {
            Log.e(TAG, "Backup file not started..");
            UploadNextFile();
        }
    }
}
