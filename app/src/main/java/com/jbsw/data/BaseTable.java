package com.jbsw.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BaseTable
{
    private static final String TAG = "TAGBaseTable";
    protected Cursor m_Cur = null;

    public int GetRecordCount()
    {
        return m_Cur.getCount();
    }

    public long DoQuery(String sQuery)
    {
        DBManager DBM = DBManager.Get();
        SQLiteDatabase DB = DBM.getWritableDatabase();
        m_Cur = DB.rawQuery(sQuery, null);
        int nResults = m_Cur.getCount();
        Log.d(TAG, "QueryCount: " + nResults);
        if (nResults  <= 0)
        {
            m_Cur.close();
            DB.close();
            return -1;
        }

        return nResults;
    }

    protected static boolean DoDelete(SQLiteDatabase DB, String TABLE_NAME, String COLUMN_ID, long Id)
    {
        String sId = String.format("%d", Id);
        String[] Args = {sId};
        int nRes = 0;
        try {
            nRes = DB.delete(TABLE_NAME, COLUMN_ID + " = ?", Args);
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
            return false;
        }
        return true;
    }
}
