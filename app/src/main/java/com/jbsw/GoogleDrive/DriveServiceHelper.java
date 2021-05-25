/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jbsw.GoogleDrive;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
//import android.support.v4.util.Pair;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
//import java.util.concurrent.Executors;

import androidx.core.util.Pair;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private static final String TAG = "TAGBKPDriveService";


    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    private static String getMimeType(String fileUrl)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public String UploadFile(String sFile, String sFolderId, MediaHttpUploaderProgressListener Listener)
    {
        File fileMetadata = new File();
        java.io.File SourceFile = new java.io.File(sFile);
        fileMetadata.setName(SourceFile.getName());
        fileMetadata.setParents(Collections.singletonList(sFolderId));
        String sMime = getMimeType(sFile);
        Log.d(TAG, " SOurce File name: " + SourceFile.getName() + " MIME: " + sMime);
//        fileMetadata.setMimeType(sMime);

        try {
            InputStreamContent mediaContent = new InputStreamContent(sMime,  new BufferedInputStream(new FileInputStream(SourceFile)));
            mediaContent.setLength(SourceFile.length());
            HttpResponse file = mDriveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .getMediaHttpUploader()
                    .setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE)
                    .setProgressListener(Listener)
                    .setDirectUploadEnabled(false)
                    .upload(new GenericUrl("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"));
        }
        catch (Exception e)
        {
            Log.e(TAG, "Cant Create Google Drive File for: " + sFile + " , Exception: " + e.getMessage());
        }
        return null;
    }

    public Task<String> createFolder(String sName)
    {
        return Tasks.call(mExecutor, () -> {
            return AsyncCreateFolder(sName);
        });
    }

    public String AsyncCreateFolder(String sName)
    {
        File fileMetadata = new File();
        fileMetadata.setName(sName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        try {
            File file = mDriveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            return file.getId();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Cant Create Google Drive Folder, Exception: " + e.getMessage());
        }

        return "";
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }

    public String FindFolder(String sName)
    {
        String sQry = String.format("mimeType = 'application/vnd.google-apps.folder' and name='%s'", sName);
        String sId = null;

        try {
            String pageToken = null;
            do {
                FileList files = mDriveService.files().list()
                        .setQ(sQry)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : files.getFiles()) {
                    sId = file.getId();
                    Log.d(TAG, "Folder found: " + file.getName());
                }
                pageToken = files.getNextPageToken();
            } while (pageToken != null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to find file: " + e.getMessage());
        }

        return sId;
    }

    public boolean DeleteFie(String sFileId)
    {
        try {
            mDriveService.files().delete(sFileId).execute();
        } catch (IOException e) {
            Log.e(TAG, "An error occurred Deleting the file: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }
}