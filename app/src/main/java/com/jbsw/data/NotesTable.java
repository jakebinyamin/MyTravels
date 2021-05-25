package com.jbsw.data;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.jbsw.utils.GpsTracker;
import com.jbsw.utils.Prefs;
import com.jbsw.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesTable extends BaseTable
{
    private static final String TAG = "TAGNotesTable";

    //
    // Table Name
    private static final String 	TABLE_NOTES = "Notes";
    private static double m_Longitude = -1, m_latitude = -1;

    //
    // Columns
    private static final String 	COLUMN_ID 		    = "_id";
    private static final String 	COLUMN_MASTERID	    = "_idMaster";
    private static final String 	COLUMN_DATE		    = "Date";
    private static final String 	COLUMN_TYPE 	    = "Type";
    private static final String     COLUMN_TITLE        = "Title";
    private static final String     COLUMN_DATA         = "Data";
    private static final String     COLUMN_lONG         = "Longitude";
    private static final String     COLUMN_LAT          = "Latitude";
    private static final String     COLUMN_SHOW_ONMAP   = "ShowOnMap";

    //
    // Create Table
    public static final String CREATE_TABLE_NOTES = "create table "
            + TABLE_NOTES + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_MASTERID + " integer not null, "
            + COLUMN_DATE + " date not null, "
            + COLUMN_TYPE + " integer, "
            + COLUMN_TITLE + " text, "
            + COLUMN_DATA + " blob, "
            + COLUMN_lONG + " double, "
            + COLUMN_LAT + " double, "
            + COLUMN_SHOW_ONMAP + " integer "
            + ");";

    private static final String UPDATE_TABLE_WHERE_CLAUSE = COLUMN_ID + " = %d"; // AND " + COLUMN_DATE + "  = \"%s\"";

    private static final String QUERY_TABLE_NOTES = "select * from " + TABLE_NOTES + " where " + COLUMN_MASTERID + " = ";
    private static final String QUERY_TABLE_NOTE = "select * from " + TABLE_NOTES + " where " + COLUMN_ID + " = "; //%d AND " + COLUMN_DATE + " = \"%s\"";
    private static final String WHERECLAUSE_FOR_MAP = " and " + COLUMN_SHOW_ONMAP + " = 1";
    private static final String WHERECLAUSE_FOR_DATE = " and " + COLUMN_DATE + " > \"%s\" and " + COLUMN_DATE + " < \"%s\"";
    public static final String ORDERBY_CLAUSE = " order by " + COLUMN_ID;

    //
    // Data Types
    public static final int    DataText          = 0;
    public static final int    DataFlight        = 1;
    public static final int    DataMeal          = 2;
    public static final int    DataRelaxing      = 3;
    public static final int    DataExplore       = 4;
    public static final int    DataAccommodation = 5;
    public static final int    DataShopping      = 6;
    public static final int    DataTravel        = 7;
    public static final int    DataBusiness      = 8;
    public static final int    DataWork          = 9;
    public static final int    DataSport         = 10;
    public static final int    DataExcercise     = 11;
    public static final int    DataHike          = 12;
    public static final int    DataSocial        = 13;
    public static final int    DataFun           = 14;
    public static final int    MAX_TYPES         = 15;

    public class DataRecord
    {
        public long Id;
        public long MasterId;
        public String Date;
        public int nType;
        public String sTitle;
        public String sStringNote;
        public double nLongitude;
        public double nLatitude;
        public boolean bShowOnMap;
    }

    public DataRecord GetNewDataRecord()
    {
        DataRecord DR = new DataRecord();
        DR.nLatitude = -1;
        DR.nLongitude = -1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        DR.Date = sdf.format(new Date());
        DR.bShowOnMap = true;

        return DR;
    }

    public long CreateTextNote(DataRecord DR)
    {
        DBManager DBM = DBManager.Get();
        long retVal = -1;

//        GpsTracker Tracker = new GpsTracker();
//        Location Loc = Tracker.GetLocation();

        GpsTracker gps = GpsTracker.GetTracker();
        if (gps != null) {
            Location loc = gps.GetLocation();
            if (loc != null) {
                m_Longitude = loc.getLongitude();
                m_latitude = loc.getLatitude();
            }
        }

//        if (DR.nLongitude == -1)
            DR.nLongitude = m_Longitude;
//        if (DR.nLatitude == -1)
            DR.nLatitude = m_latitude;

        if (m_Longitude == -1 && m_latitude == -1)
            Log.e(TAG, "Longitude and Latitude not set!!");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date = sdf.format(new Date());
            byte[] StringBytes = DR.sStringNote.getBytes();

            ContentValues values = new ContentValues();
            values.put(COLUMN_MASTERID, DR.MasterId);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_TYPE, DR.nType);
            values.put(COLUMN_TITLE, DR.sTitle);
            values.put(COLUMN_DATA, StringBytes);
            values.put(COLUMN_lONG, DR.nLongitude);
            values.put(COLUMN_LAT, DR.nLatitude);
            values.put(COLUMN_SHOW_ONMAP, DR.bShowOnMap ? 1 : 0);

            SQLiteDatabase DB = DBM.getWritableDatabase();
            retVal = DB.insert(TABLE_NOTES, null, values);
            DB.close();
        } catch (SQLException e) {
            Log.d(TAG, "CreateRecord failed " + retVal);
            e.printStackTrace();
            return -1;
        }
        Log.d(TAG, "CreateRecord RetVal: " + retVal);
        return retVal;
    }

//    public static class gpsCoordinateReceiver extends BroadcastReceiver
//    {
//        @Override
//        public void onReceive(Context context, Intent intent)
//        {
//            m_Longitude = intent.getDoubleExtra("Longitude", -1);
//            m_latitude = intent.getDoubleExtra("Latitude", -1);
//            Log.d(TAG, "Broadcast receiver, Longitude: " + m_Longitude + " Latitude " + m_latitude);
//        }
//    }

    public boolean UpdateRecord(DataRecord DR)
    {
        DBManager DBM = DBManager.Get();
        long retVal = -1;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date = sdf.format(new Date());
            byte[] StringBytes = DR.sStringNote.getBytes();

            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, DR.sTitle);
            values.put(COLUMN_DATA, StringBytes);
            values.put(COLUMN_TYPE, DR.nType);
            values.put(COLUMN_lONG, DR.nLongitude);
            values.put(COLUMN_LAT, DR.nLatitude);
            values.put(COLUMN_SHOW_ONMAP, DR.bShowOnMap ? 1 : 0);

            SQLiteDatabase DB = DBM.getWritableDatabase();
            String sWhere = String.format(UPDATE_TABLE_WHERE_CLAUSE, DR.Id); //, DR.Date);
            retVal = DB.update(TABLE_NOTES, values, sWhere, null);
            DB.close();
        } catch (SQLException e) {
            Log.d(TAG, "CreateRecord failed " + retVal);
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "CreateRecord RetVal: " + retVal);
        return true;
    }

    public long QueryAllForId(long id)
    {
        String sBaseQry = QUERY_TABLE_NOTES + id;
        String sQuery = sBaseQry + ORDERBY_CLAUSE;
        DBManager DBM = DBManager.Get();
        Prefs p = new Prefs(DBM.GetContext());
        if (p.GetSortOrderJournal() == Prefs.JourneySortByNewest)
            sQuery += " desc";

        return DoQuery(sQuery);
    }

    public long QueryAllForMap(long id, String sDate)
    {
        String sQuery = QUERY_TABLE_NOTES + id + WHERECLAUSE_FOR_MAP;
        if (sDate != null)
        {
            sDate = Utils.GetDateWithNoTime(sDate);
            String sDateTo = Utils.GetDatePlusDays(sDate, 1);
            String sDateQuery = String.format(WHERECLAUSE_FOR_DATE, sDate, sDateTo);
            sQuery += sDateQuery;
        }
        return DoQuery(sQuery);
    }

    public DataRecord GetJournalEntry(long id)
    {
        DBManager DBM = DBManager.Get();
        SQLiteDatabase DB = DBM.getWritableDatabase();
        String sQuery = QUERY_TABLE_NOTE + id;
        Log.d(TAG, "Single Query Record: " + sQuery);
        m_Cur = DB.rawQuery(sQuery, null);
        if ( m_Cur.getCount() <= 0)
            return null;

        DataRecord DR = GetNextRecord();
        m_Cur.close();
        return DR;
    }

    public DataRecord GetDataAtPosition(int nPos)
    {
        if (m_Cur == null)
            return null;

        if (!m_Cur.moveToPosition(nPos))
            return null;

        return GetData();
    }

    public DataRecord GetNextRecord()
    {
        if (m_Cur == null)
            return null;

        if (!m_Cur.moveToNext())
            return null;

        return GetData();
    }

    public DataRecord GetData()
    {
        DataRecord DR = new DataRecord();
        DR.Id = m_Cur.getLong(0);
        DR.MasterId = m_Cur.getLong(1);
        DR.Date = m_Cur.getString(2);
        DR.nType = m_Cur.getInt(3);
        DR.sTitle = m_Cur.getString(4);
        byte[] Bytes = m_Cur.getBlob(5);
        DR.sStringNote = new String(Bytes, StandardCharsets.UTF_8);
        DR.nLongitude = m_Cur.getDouble(6);
        DR.nLatitude = m_Cur.getDouble(7);
        DR.bShowOnMap = m_Cur.getInt(8) > 0;
        return DR;
    }

    public static boolean DeleteRecord(SQLiteDatabase DB, long Id)
    {
        DoDelete(DB, TABLE_NOTES, COLUMN_MASTERID, Id);
        return true;
    }

    public static boolean DeleteNoteRecord(SQLiteDatabase DB, long Id)
    {
        DoDelete(DB, TABLE_NOTES, COLUMN_ID, Id);
        return true;
    }

}
