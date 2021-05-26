package com.jbsw.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import com.jbsw.utils.GpsTracker;
import com.jbsw.utils.Prefs;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TravelMasterTable extends BaseTable
{
    private static final String TAG = "TAGTravelMasterTable";

    //
    // Table Name
    private static final String 	TABLE_MASTER = "Master";

    //
    // Columns
    private static final String 	COLUMN_ID 		 = "_id";
    private static final String 	COLUMN_NAME		 = "Name";
    private static final String 	COLUMN_DESCR	 = "Description";
    private static final String     COLUMN_STATUS    = "Status";
    private static final String 	COLUMN_START	 = "StartDate";
    private static final String 	COLUMN_END		 = "EndDate";
    private static final String     COLUMN_PHOTO     = "Photo";
    private static final String     COLUMN_USEGPS    = "UseGps";

    //
    // Create Table
    public static final String CREATE_TABLE_MASTER = "create table "
            + TABLE_MASTER + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_DESCR + " text, "
            + COLUMN_STATUS + " integer, "
            + COLUMN_START + " date, "
            + COLUMN_END + " date, "
            + COLUMN_PHOTO + " string, "
            + COLUMN_USEGPS + " integer "
            + ");";

    public static final String UPDATE_TABLE_MASTER = "update " + TABLE_MASTER + " SET "
        + COLUMN_NAME + " = \"%s\", "
        + COLUMN_DESCR + " = \"%s\", "
        + COLUMN_STATUS + " = %d, "
        + COLUMN_START + " = \"%s\", "
        + COLUMN_END + " = \"%s\", "
        + COLUMN_USEGPS + " = %d "
        + " WHERE " + COLUMN_ID + " = %d";

    private static final String UPDATE_PHOTO = "update " + TABLE_MASTER + " SET "
            + COLUMN_PHOTO + " = \"%s\" "
            + " WHERE " + COLUMN_ID + " = %d";

    //
    // Status Types
    public static final int    StatusNotStarted     = 0;
    public static final int    StatusInProgress     = 1;
    public static final int    StatusPaused         = 2;
    public static final int    StatusComplete       = 3;

    public static final String QUERY_TABLE_MASTER_ALL = "select * from " + TABLE_MASTER;
    public static final String ORDERBY_CLAUSE = " order by " + COLUMN_ID;
    public static final String QUERY_TABLE_MASTER_INPROGRESS = "select * from " + TABLE_MASTER + " where " + COLUMN_STATUS + " = " + StatusInProgress + " and " + COLUMN_USEGPS + " = 1";
    public static final String QUERY_TABLE_MASTER_RECORD = QUERY_TABLE_MASTER_ALL + " where " + COLUMN_ID + " = ";

    public class DataRecord
    {
        public long Id;
        public String Name;
        public String Descr;
        public int Status;
        public String StartDate;
        public String EndDate;
        public String sPhoto;
        public boolean UseGps;
    }

    public static long CreateRecord(String Name, String Description, boolean bUseGps)
    {
        DBManager DBM = DBManager.Get();
        long retVal = -1;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(new Date());

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, Name);
            values.put(COLUMN_DESCR, Description);
            //values.put(COLUMN_START, date);
            values.put(COLUMN_STATUS, StatusNotStarted);
            values.put(COLUMN_PHOTO, "");
            values.put(COLUMN_USEGPS, bUseGps ? 1 : 0);

            SQLiteDatabase DB = DBM.getWritableDatabase();
            retVal = DB.insert(TABLE_MASTER, null, values);
            DB.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        Log.d(TAG, "CreateRecord RetVal: " + retVal);
        return retVal;
    }

    public DataRecord QueryRecord(long id)
    {
        DBManager DBM = DBManager.Get();
        if (DBM == null)
        {
            Log.d(TAG, "DDBM is NULL!!!!!!!!");
            return null;
        }
        SQLiteDatabase DB = DBM.getWritableDatabase();
        String sQuery = QUERY_TABLE_MASTER_RECORD + id;
        Log.d(TAG, "Query Record: " + sQuery);
        m_Cur = DB.rawQuery(sQuery, null);
        if ( m_Cur.getCount() <= 0)
            return null;

        DataRecord DR = GetNextRecord();
        m_Cur.close();
        return DR;
    }

    public boolean QueryAllInProgress()
    {
        return DoQuery(QUERY_TABLE_MASTER_INPROGRESS) > 0;
    }
    public boolean QueryRaw()
    {
        return  DoQuery(QUERY_TABLE_MASTER_ALL) > 0;
    }

    public boolean QueryAll()
    {
        String sQuery = QUERY_TABLE_MASTER_ALL + ORDERBY_CLAUSE;
        DBManager DBM = DBManager.Get();
        Prefs p = new Prefs(DBM.GetContext());
        if (p.GetSortOrderJourney() == Prefs.JourneySortByNewest)
            sQuery += " desc";

        return DoQuery(sQuery) > 0;
    }

//    private boolean DoQueryAll(String sQuery)
//    {
//        DBManager DBM = DBManager.Get();
//        SQLiteDatabase DB = DBM.getWritableDatabase();
//        m_Cur = DB.rawQuery(sQuery, null);
//        int count = m_Cur.getCount();
//        Log.d(TAG, "Database Count: " + count);
//        return (count > 0);
//    }

    public DataRecord GetNextRecord()
    {
        if (m_Cur == null)
            return null;

        if (!m_Cur.moveToNext())
            return null;

        return GetData();
    }

    public DataRecord GetDataAtPosition(int nPos)
    {
        if (m_Cur == null)
            return null;

        if (!m_Cur.moveToPosition(nPos))
            return null;

        return GetData();
    }

    public DataRecord GetData()
    {
        DataRecord DR = new DataRecord();
        DR.Id = m_Cur.getInt(0);
        DR.Name = m_Cur.getString(1);
        DR.Descr = m_Cur.getString(2);
        DR.Status = m_Cur.getInt(3);
        DR.StartDate = m_Cur.getString(4);
        DR.EndDate = m_Cur.getString(5);
        DR.sPhoto = m_Cur.getString(6);
        DR.UseGps = m_Cur.getInt(7) > 0 ? true : false;

        return DR;
    }

    public boolean UpdateRecord(DataRecord DR)
    {
        Log.d(TAG, "Name: " + DR.Name + " Descr: " + DR.Descr);
        String sUpdateQry = String.format(UPDATE_TABLE_MASTER, DR.Name, DR.Descr, DR.Status, DR.StartDate, DR.EndDate, DR.UseGps ? 1: 0,  DR.Id);
        Log.d(TAG, "Update Query: " + sUpdateQry);
        boolean bRetVal =  DoUpdate(sUpdateQry);
        if (bRetVal) {
            GpsTracker gps  = GpsTracker.GetTracker();
            if (gps != null)
                gps.CheckToStartGPSMonitor();
        }
        return bRetVal;
    }

    public boolean UpdatePhoto(long Id, String sPhoto)
    {
        String sUpdateQry = String.format(UPDATE_PHOTO, sPhoto, Id);
        return DoUpdate(sUpdateQry);
    }


    private boolean DoUpdate(String sSQL)
    {
        try {
            DBManager DBM = DBManager.Get();
            SQLiteDatabase DB = DBM.getWritableDatabase();
            DB.execSQL(sSQL);
            DB.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean DeleteRecord(SQLiteDatabase DB, long Id)
    {
        return DoDelete(DB, TABLE_MASTER, COLUMN_ID, Id);
    }

}
