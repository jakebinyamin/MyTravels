package com.jbsw.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.provider.ContactsContract;
import android.util.Log;

import com.jbsw.utils.Prefs;
import com.jbsw.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GpsDataTable extends BaseTable
{
    private static final String TAG = "TAGGpsDataTable";

    //
    // Table Name
    private static final String 	TABLE_GPS = "GPSData";

    //
    // Columns
    private static final String 	COLUMN_ID 		 = "_id";
    private static final String 	COLUMN_DATE		 = "Date";
    private static final String     COLUMN_LONG      = "Longitude";
    private static final String     COLUMN_LAT       = "Latitude";

    //
    // Create Table
    public static final String CREATE_TABLE_GPS_DATA = "create table "
            + TABLE_GPS + "("
            + COLUMN_ID + " integer not null, "
            + COLUMN_DATE + " date not null, "
            + COLUMN_LONG + " double, "
            + COLUMN_LAT + " double "
            + ");";

    private static final String QUERY_TABLE_GPS = "select * from " + TABLE_GPS + " where " + COLUMN_ID + " = %d";
    private static final String WHERECLAUSE_FOR_DATE = " and " + COLUMN_DATE + " > \"%s\" and " + COLUMN_DATE + " < \"%s\"";

    public class DataRecord
    {
        public long Id;
        public String Date;
        public double nLongitude;
        public double nLatitude;
    }

    public boolean AddGPSRecord(long Id, Location Loc)
    {
        if (Loc == null)
            return false;

        DBManager DBM = DBManager.Get();
        long retVal = -1;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date = sdf.format(new Date());

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, Id);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_LONG, Loc.getLongitude());
            values.put(COLUMN_LAT, Loc.getLatitude());

            SQLiteDatabase DB = DBM.getWritableDatabase();
            retVal = DB.insert(TABLE_GPS, null, values);
            DB.close();
            Prefs prefs = new Prefs(DBM.GetContext());
            prefs.MarkJournalChange();
        } catch (SQLException e) {
            Log.d(TAG, "CreateRecord failed " + retVal);
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "CreateRecord RetVal: " + retVal);
        return true;
    }

    public long QueryAll(long id)
    {
        String sQuery = String.format(QUERY_TABLE_GPS, id);
        return DoQuery(sQuery);
    }

    public long QueryForDate(long Id, String sDate)
    {
        sDate = Utils.GetDateWithNoTime(sDate);
        String sDateTo = Utils.GetDatePlusDays(sDate, 1);
        String sQuery = String.format(QUERY_TABLE_GPS + WHERECLAUSE_FOR_DATE, Id, sDate, sDateTo);
        return DoQuery(sQuery);
    }

    public DataRecord GetNextRecord()
    {
        if (!m_Cur.moveToNext())
            return null;

        return GetData();
    }

    public DataRecord GetData()
    {
        DataRecord DR = new DataRecord();
        DR.Id = m_Cur.getInt(0);
        DR.Date = m_Cur.getString(1);
        DR.nLongitude = m_Cur.getDouble(2);
        DR.nLatitude = m_Cur.getDouble(3);
        return DR;
    }

    public static boolean DeleteRecord(SQLiteDatabase DB, long Id)
    {
        DoDelete(DB, TABLE_GPS, COLUMN_ID, Id);
        return true;
    }
}
