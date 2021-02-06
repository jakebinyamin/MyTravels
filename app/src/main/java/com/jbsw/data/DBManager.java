package com.jbsw.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DBManager  extends SQLiteOpenHelper
{
    private static final String TAG = "TAGDBManager";

    private static DBManager        m_Singelton = null;
    private static final String 	DATABASE_NAME 	 = "MyTravels.db";
    private static final int 		DATABASE_VERSION = 2;

    private SQLiteDatabase 	m_Database = null;
    private Context         m_Context = null;

    public SQLException lastDBOpenError = null;

    public DBManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        if (m_Singelton != null)
            m_Singelton = null;

        m_Context = context;
        m_Singelton = this;
    }

    static public DBManager Create(Context context)
    {
        return new DBManager(context);
    }

    static public DBManager Get()
    {
        return m_Singelton;
    }

    public Context GetContext()
    {
        return m_Context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(TravelMasterTable.CREATE_TABLE_MASTER);
        db.execSQL(NotesTable.CREATE_TABLE_NOTES);
        db.execSQL(GpsDataTable.CREATE_TABLE_GPS_DATA);
        db.execSQL(PhotoLinkTable.CREATE_TABLE_PHOTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion < 2)
            db.execSQL("ALTER TABLE Notes ADD COLUMN ShowOnMap INTEGER DEFAULT 1");
    }

    public boolean Open()
    {
        try {
            if (m_Database == null)
                m_Database = getWritableDatabase();
            return true;
        } catch (SQLException e) {
            lastDBOpenError = e;
            e.printStackTrace();
        }

        return false;
    }

    public void Close()
    {
        m_Database.close();
        close();
    }

    public boolean DeleteTrip(long Id)
    {
        SQLiteDatabase DB = getWritableDatabase();
        if (!TravelMasterTable.DeleteRecord(DB, Id))
            return false;

        NotesTable.DeleteRecord(DB, Id);
        PhotoLinkTable.DeleteRecord(DB, Id);
        GpsDataTable.DeleteRecord(DB, Id);

        return true;
    }

    public boolean DeleteJournalEntry(long Id)
    {
        SQLiteDatabase DB = getWritableDatabase();
        PhotoLinkTable.DeleteNoteRecord(DB, Id);
        NotesTable.DeleteNoteRecord(DB, Id);
        return true;
    }
}
