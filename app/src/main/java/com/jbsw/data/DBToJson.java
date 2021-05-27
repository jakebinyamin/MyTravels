package com.jbsw.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class DBToJson
{
    private static final String TAG = "TAGBKPDBToJson";

    private JSONObject m_JsonRoot;
    private JSONArray m_JsonMasterRecs;
    Context m_Context;
    String m_sOutFile;
    int m_nPhotoCnt;

    HashMap<String, String> m_UploadFileList;

    public DBToJson(Context context)
    {
        m_Context = context;
        m_JsonRoot = new JSONObject();
        m_JsonMasterRecs = new JSONArray();
        m_UploadFileList = new HashMap<String, String>();
        m_nPhotoCnt = 0;
    }

    public String GetJsonFile()
    {
        return m_sOutFile;
    }

    public HashMap<String, String> GetFilesToUpload()
    {
        return m_UploadFileList;
    }

    public boolean Export()
    {
        String sOutFile = null;
        boolean bRetVal = false;

        //
        // Get all Master records
        TravelMasterTable Master = new TravelMasterTable();
        if (!Master.QueryRaw())
            return bRetVal;

        TravelMasterTable.DataRecord DRMaster;
        while ((DRMaster = Master.GetNextRecord()) != null)
        {
            try {
                JSONObject MasterRec = new JSONObject();
                MasterRec.put("Name", DRMaster.Name);
                MasterRec.put("Description", DRMaster.Descr);
                MasterRec.put("Status", DRMaster.Status);
                MasterRec.put("StartDate", DRMaster.StartDate);
                MasterRec.put("EndDate", DRMaster.EndDate);
                MasterRec.put("usegps", DRMaster.UseGps);

                String sNewPhoto = "";
                if (!DRMaster.sPhoto.isEmpty())
                    sNewPhoto = CreatePhotoName(DRMaster.sPhoto);
                MasterRec.put("Photo", sNewPhoto);

                m_JsonMasterRecs.put(MasterRec);

                AddJournalEntries(MasterRec, DRMaster.Id);
                AddGPSEntries(MasterRec, DRMaster.Id);
            }
            catch (Exception e) {
                Log.e(TAG, "Problem Creating JSON Record: " + e.getMessage());
            }
        }

        //
        // Export the json to a file
        File fJSON = new File(m_Context.getFilesDir(), "MTDBExport.json");
        m_sOutFile = fJSON.getAbsolutePath();
        try {
            FileWriter file = new FileWriter(m_sOutFile);
            m_JsonRoot.put("Master", m_JsonMasterRecs);
            file.write(m_JsonRoot.toString(1));
            file.flush();
            Log.d(TAG, "JSON Created: " + m_sOutFile + "\n\n\n"  + m_JsonRoot.toString(1));
            java.io.File OutFile = new java.io.File(m_sOutFile);
            m_UploadFileList.put(m_sOutFile, OutFile.getName());

            bRetVal = true;
        }
        catch (Exception e) {
            Log.e(TAG, "Cant write json file: " + e.getMessage());
            bRetVal = false;
        }

        return bRetVal;
    }

    private String CreatePhotoName(String sPhoto)
    {
        String sNewFileName = m_UploadFileList.get(sPhoto);
        if (sNewFileName != null) {
            Log.d(TAG, "Photo already exists" + sPhoto + " " + sNewFileName);
            return sNewFileName;
        }

        String sExt = null;
        int index = sPhoto.lastIndexOf('.');
        if(index > 0) {
            sExt = sPhoto.substring(index);
        }

        //
        // Add this file to the HashTable
        sNewFileName = String.format("%d%s", m_nPhotoCnt, sExt);
        m_UploadFileList.put(sPhoto, sNewFileName);
        Log.d(TAG, "Source Photo: " + sPhoto + " New Photo file: " + sNewFileName);
        m_nPhotoCnt++;

        return sNewFileName;
    }


    private void AddJournalEntries(JSONObject jMaster, long nIdMaster)
    {
        NotesTable Journals = new NotesTable();
        if (Journals.QueryAllForId(nIdMaster) < 0)
            return;

        try {
            JSONArray JSONNotes = new JSONArray();;
            NotesTable.DataRecord JRecord;
            while ((JRecord = Journals.GetNextRecord()) != null)
            {
                JSONObject JournalRec = new JSONObject();
                JournalRec.put("date", JRecord.Date);
                JournalRec.put("type", JRecord.nType);
                JournalRec.put("title", JRecord.sTitle);
                JournalRec.put("note", JRecord.sStringNote);
                JournalRec.put("Longitude", JRecord.nLongitude);
                JournalRec.put("Latitude", JRecord.nLatitude);
                JournalRec.put("showonmap", JRecord.bShowOnMap);

                JSONNotes.put(JournalRec);
                AddJournalPhotos(JournalRec, JRecord.Id);
            }
            jMaster.put("Journals", JSONNotes);
        }
        catch (Exception e) {
            Log.e(TAG, "Problem Creating JSON Journal Entry: " + e.getMessage());
        }

    }

    private void AddJournalPhotos(JSONObject journalRec, long Noteid)
    {
        PhotoLinkTable TabPhoto = new PhotoLinkTable();
        if (TabPhoto.QueryForNoteEntry(Noteid) < 0)
            return;
        try {
            JSONArray JSONPics = new JSONArray();
            PhotoLinkTable.DataRecord RecPhoto;
            while ((RecPhoto = TabPhoto.GetNextRecord()) != null)
            {
                JSONObject JPhoto = new JSONObject();
                String sPhoto = CreatePhotoName(RecPhoto.sPath);
                JSONPics.put(sPhoto);
            }

            journalRec.put("Photos", JSONPics);
        }
        catch (Exception e) {
            Log.e(TAG, "Problem Creating JSON For Journal Photos: " + e.getMessage());
        }
    }

    private void AddGPSEntries(JSONObject jMaster, long nIdMaster)
    {
        GpsDataTable Gps = new GpsDataTable();
        if (Gps.QueryAll(nIdMaster) < 0)
            return;

        try {
            JSONArray JSONGps = new JSONArray();
            GpsDataTable.DataRecord GpsRec;
            while ((GpsRec = Gps.GetNextRecord()) != null)
            {
                JSONObject Jgps = new JSONObject();
                Jgps.put("date", GpsRec.Date);
                Jgps.put("Longitude", GpsRec.nLongitude);
                Jgps.put("Latitude", GpsRec.nLatitude);

                JSONGps.put(Jgps);
            }

            jMaster.put("GPSTable", JSONGps);
        }
        catch (Exception e) {
            Log.e(TAG, "Problem Creating JSON GPS Entry: " + e.getMessage());
        }
    }

}
