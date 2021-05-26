package com.jbsw.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;

public class DBToJson
{
    private static final String TAG = "TAGBKPDBToJson";

    private JSONObject m_JsonRoot;
    private JSONArray m_JsonMasterRecs;
    Context m_Context;
    String m_sOutFile;

    public DBToJson(Context context)
    {
        m_Context = context;
        m_JsonRoot = new JSONObject();
        m_JsonMasterRecs = new JSONArray();
    }

    public String GetJsonFile()
    {
        return m_sOutFile;
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
                MasterRec.put("Photo", DRMaster.sPhoto);
                MasterRec.put("usegps", DRMaster.UseGps);

                m_JsonMasterRecs.put(MasterRec);
            }
            catch (Exception e) {
                Log.e(TAG, "Problem Creating JSON Record: " + e.getMessage());
            }
        }

        File fJSON = new File(m_Context.getFilesDir(), "MTDBExport.json");
        m_sOutFile = fJSON.getAbsolutePath();
        try {
            FileWriter file = new FileWriter(m_sOutFile);
            m_JsonRoot.put("Master", m_JsonMasterRecs);
            file.write(m_JsonRoot.toString(1));
            file.flush();
            Log.d(TAG, "JSON Created: " + m_sOutFile + "\n\n\n" + m_JsonRoot.toString(1));
            bRetVal = true;
        }
        catch (Exception e) {
            Log.e(TAG, "Cant write json file: " + e.getMessage());
            bRetVal = false;
        }

        return bRetVal;
    }
}
