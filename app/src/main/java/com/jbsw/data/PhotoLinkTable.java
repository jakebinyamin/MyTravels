package com.jbsw.data;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.nio.charset.StandardCharsets;

public class PhotoLinkTable extends BaseTable
{
    private static final String TAG = "TAGPhotoLinkTable";

    //
    // Table Name
    private static final String 	TABLE_NAME = "Photos";

    //
    // Columns
    private static final String 	COLUMN_ID 		 = "_id";
    private static final String 	COLUMN_NOTEID	 = "idNote";
    private static final String 	COLUMN_MASTERID	 = "idMaster";
    private static final String     COLUMN_PATH      = "Path";

    //
    // Create Table
    public static final String CREATE_TABLE_PHOTOS = "create table "
            + TABLE_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NOTEID + " integer not null, "
            + COLUMN_MASTERID + " integer not null, "
            + COLUMN_PATH + " text "
            + ");";

//    private static final String QUERY_TABLE_FORNOTE = "select * from " + TABLE_NAME + " where " + COLUMN_NOTEID + " = ";
    private static final String QUERY_TABLE = "select * from " + TABLE_NAME + " where ";
    private static final String WHERE_FOR_NOTE = COLUMN_NOTEID + " = ";
    private static final String WHERE_FOR_MASTER = COLUMN_MASTERID + " = ";

    public class DataRecord
    {
        public long Id;
        public long NoteId;
        public long MasterId;
        public String sPath;
    }

    public boolean AddPhoto(long MasterId, long NoteId, String sPath)
    {
        DBManager DBM = DBManager.Get();
        long retVal = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_MASTERID, MasterId);
            values.put(COLUMN_NOTEID, NoteId);
            values.put(COLUMN_PATH, sPath);

            SQLiteDatabase DB = DBM.getWritableDatabase();
            retVal = DB.insert(TABLE_NAME, null, values);
            DB.close();
        }
        catch (SQLException e)
        {
            Log.d(TAG, "CreateRecord failed " + retVal);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public long QueryForMaster(long MasterId)
    {
        String sWhereClause = WHERE_FOR_MASTER + MasterId;
        return Query(sWhereClause);
    }

    public long QueryForNoteEntry(long idNote)
    {
        String sWhereClause = WHERE_FOR_NOTE + idNote;
        return Query(sWhereClause);
    }

    private long Query(String sWhereClause)
    {
        DBManager DBM = DBManager.Get();
        SQLiteDatabase DB = DBM.getWritableDatabase();
        String sQuery = QUERY_TABLE + sWhereClause;
        m_Cur = DB.rawQuery(sQuery, null);
        int nResults = m_Cur.getCount();
        if (nResults  <= 0)
        {
            m_Cur.close();
            DB.close();
            return -1;
        }

        return nResults;
    }

    public DataRecord GetDataAtPosition(int nPos)
    {
        if (!m_Cur.moveToPosition(nPos))
            return null;

        return GetData();
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
        DR.Id = m_Cur.getLong(0);
        DR.NoteId = m_Cur.getLong(1);
        DR.MasterId = m_Cur.getLong(2);
        DR.sPath = m_Cur.getString(3);
        return DR;
    }

    public static boolean DeleteRecord(SQLiteDatabase DB, long  Id)
    {
        DoDelete(DB, TABLE_NAME, COLUMN_MASTERID, Id);
        return true;
    }

    public static boolean DeleteNoteRecord(SQLiteDatabase DB, long  Id)
    {
        DoDelete(DB, TABLE_NAME, COLUMN_NOTEID, Id);
        return true;
    }

    public boolean DeletePhotoFromNote(long nNoteId, String sPhoto)
    {
        String sId = String.format("%d", nNoteId);
        String[] Args = {sId, sPhoto};
        int nRes = 0;
        try {
            DBManager DBM = DBManager.Get();
            SQLiteDatabase DB = DBM.getWritableDatabase();
            nRes = DB.delete(TABLE_NAME, COLUMN_NOTEID + " = ? and " + COLUMN_PATH + " = ?", Args);
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
            return false;
        }

        return true;
    }
}
