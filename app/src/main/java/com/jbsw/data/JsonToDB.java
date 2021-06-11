package com.jbsw.data;

import android.content.Context;
import android.util.Log;

//import com.google.api.client.util.IOUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonToDB
{
    private static final String TAG = "TAGBKPJsonToDB";
    private Context m_Context;
    private JSONObject m_JsonRoot;

    public JsonToDB(Context context)
    {
        m_Context = context;
    }

    private boolean LoadFile(String sFile) {
        File f = new File(sFile);
        if (!f.exists())
            return false;

        boolean retVal = true;
        try {
            InputStream is = new FileInputStream(f);
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            System.out.println(jsonTxt);
            m_JsonRoot = new JSONObject(jsonTxt);
        }
        catch (Exception e) {
            Log.e(TAG, "Cannot parse the Json file: " + e.getMessage());
            retVal = false;
        }

        return retVal;
    }

    public boolean Import(String sJsonFile)
    {
        if (!LoadFile(sJsonFile))
            return false;

        boolean retVal = true;
        try {
            JSONArray Recs = m_JsonRoot.getJSONArray("Master");
            for (int i = 0; i < Recs.length(); i++)
            {
                //
                // Load the data from the JSON
                TravelMasterTable.DataRecord DR = new TravelMasterTable.DataRecord();

                JSONObject o = (JSONObject) Recs.get(i);
                DR.Name = o.getString("Name");
                DR.Descr = o.getString("Description");
                DR.Status = o.getInt("Status");
                DR.StartDate = o.getString("StartDate");
                DR.EndDate = o.getString("EndDate");
                DR.UseGps = o.getBoolean("usegps");
                String sPhoto = o.getString("Photo");

                //
                // Create a master record
                TravelMasterTable Trav = new TravelMasterTable();
                DR.Id = Trav.CreateRecord(DR.Name, DR.Descr, DR.UseGps);
                if (DR.Id > 0) {
                    Trav.UpdateRecord(DR);
                    if (sPhoto != null && !sPhoto.isEmpty()) {
                        File fPhoto = new File(m_Context.getFilesDir(), sPhoto);
                        DR.sPhoto = fPhoto.getAbsolutePath();
                        Trav.UpdatePhoto(DR.Id, DR.sPhoto);
                    }

                    GetJournals(DR.Id, o);
                    GetGpsLocations(DR.Id, o);
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Problem parsing  Master record- " + e.getMessage());
            retVal = false;
        }

        return retVal;
    }


    private void GetJournals(long MasterId, JSONObject jMasterRec)
    {
        try {
            JSONArray Recs = jMasterRec.getJSONArray("Journals");
            for (int i = 0; i < Recs.length(); i++)
            {
                NotesTable.DataRecord DR = new NotesTable.DataRecord();
                JSONObject o = (JSONObject) Recs.get(i);
                DR.MasterId = MasterId;
                DR.Date = o.getString("date");
                DR.nType = o.getInt("type");
                DR.sTitle = o.getString("title");
                DR.sStringNote = o.getString("note");
                DR.nLongitude = o.getDouble("Longitude");
                DR.nLatitude = o.getDouble("Latitude");
                DR.bShowOnMap = o.getBoolean("showonmap");

                NotesTable Note = new NotesTable();
                DR.Id = Note.CreateTextNote(DR, true);

                if (DR.Id > 0) {
                    Log.d(TAG, "Journal record created: " + MasterId + ", " + DR.Id + DR.sTitle);
                    GetJournalPhotos(MasterId, DR.Id, o);
                }
            }

        }
        catch (Exception e)
        {
            Log.e(TAG, "Problem parsing Journals - " + e.getMessage());
        }
    }

    private void GetJournalPhotos(long MasterId, long NoteId, JSONObject jJournal)
    {
        JSONArray Recs = null;
        try {
            Recs =jJournal.getJSONArray("Photos");
            Log.d(TAG, "Photos for: " + MasterId + ", "+ NoteId + ", "+ Recs.length());
            for (int i = 0; i < Recs.length(); i++)
            {
                String sPhoto = Recs.getString(i);
                File fPhoto = new File(m_Context.getFilesDir(), sPhoto);
                String sPhotoPath = fPhoto.getAbsolutePath();

                PhotoLinkTable PhotoTable = new PhotoLinkTable();
                PhotoTable.AddPhoto(MasterId, NoteId, sPhotoPath);
                Log.d(TAG, "Photo added: " + MasterId + ", " + NoteId + ", " + sPhotoPath);
            }
        }
        catch (Exception e) {
            if (Recs != null)
                Log.e(TAG, "Problem parsing Photos for - " + MasterId + ", " + NoteId + e.getMessage());
            else
                Log.e(TAG, e.getMessage());
        }
    }

    private void GetGpsLocations(long MasterId, JSONObject jMasterRec)
    {
        try {
            JSONArray Recs =jMasterRec.getJSONArray("GPSTable");
            for (int i = 0; i < Recs.length(); i++)
            {
                JSONObject o = (JSONObject) Recs.get(i);
                String sDate = o.getString("date");
                double nLong = o.getDouble("Longitude");
                double nLat = o.getDouble("Latitude");
                Log.d(TAG, "GPS Read added: " + MasterId + ", " + sDate + ", " + nLong + ", " + nLat);

                GpsDataTable TabGps = new GpsDataTable();
                TabGps.AddGpsRecordRaw(MasterId, sDate, nLong, nLat);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Problem parsing GPS for - " + MasterId + ", " + e.getMessage());
        }
    }
}
