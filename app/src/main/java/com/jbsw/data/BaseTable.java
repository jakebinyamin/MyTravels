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
        if (m_Cur == null)
            return 0;

        return m_Cur.getCount();
    }

    public synchronized long DoQuery(String sQuery)
    {
        DBManager DBM = DBManager.Get();
        if (DBM == null) {
            Log.e("TAG", "DBManager not initialised");
            return -1;
        }

        int nResults = -1;
        try {
            SQLiteDatabase DB = DBM.getWritableDatabase();
            m_Cur = DB.rawQuery(sQuery, null);
            nResults = m_Cur.getCount();
            Log.d(TAG, "QueryCount: " + nResults);
            if (nResults <= 0) {
                m_Cur.close();
 //               DB.close();
                return -1;
            }
        }
        catch (IllegalStateException e) {
            nResults = -1;
            Log.e(TAG,"IllegalStateException: " + e.getMessage());
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
