package com.jbsw.utils;

import android.content.SharedPreferences;
import android.content.Context;

import static android.content.Context.MODE_PRIVATE;

public class Prefs
{
    private SharedPreferences m_Pref;
    private static final String PrefJournalChange = "JOURNALCHANGE";
    private static final String PrefGps = "gps";
    private static final String PrefShowIntro = "showIntro";
    private static final String PrefSortJourney = "sort_order_journey";
    private static final String PrefSortJournal = "sort_order_journal";

    public static final int JourneySortByOldest = 0;
    public static final int JourneySortByNewest = 1;

    public static final long[] GpsTrackerData = { 500, 300, 100, 50};

    public Prefs(Context context)
    {
        m_Pref = context.getSharedPreferences("MyPref", MODE_PRIVATE);
    }

    public void MarkJournalChange()
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        editor.putBoolean(PrefJournalChange, true);
        editor.commit();
    }

    public boolean HasJournalChanged()
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        boolean bRetVal = m_Pref.getBoolean(PrefJournalChange, false);
        editor.putBoolean(PrefJournalChange, false);
//        editor.remove(PrefJournalChange);
        editor.commit();
        return bRetVal;
    }

    public void SetGpsTracker(int nDistance)
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        editor.putInt(PrefGps, nDistance);
        editor.commit();
    }

    public int GetGpsTracker()
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        int nRetVal = m_Pref.getInt(PrefGps, 1);
        return nRetVal;
    }

    public long GetGpsTrackerData()
    {
        int nPos = GetGpsTracker();
        return GpsTrackerData[nPos];
    }

    public void SetSortOrderJourney(int nPref)
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        editor.putInt(PrefSortJourney, nPref);
        editor.commit();
    }

    public int GetSortOrderJourney()
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        int nRetVal = m_Pref.getInt(PrefSortJourney, JourneySortByOldest);
        return nRetVal;
    }

    public void SetSortOrderJournal(int nPref)
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        editor.putInt(PrefSortJournal, nPref);
        editor.commit();
    }

    public int GetSortOrderJournal()
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        int nRetVal = m_Pref.getInt(PrefSortJournal, JourneySortByOldest);
        return nRetVal;
    }

    public boolean ShowIntro()
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        int nRetVal = m_Pref.getInt(PrefShowIntro, 1);
        return nRetVal > 0;
    }

    public void SetIntroShown()
    {
        SharedPreferences.Editor editor = m_Pref.edit();
        editor.putInt(PrefShowIntro, 0);
        editor.commit();
    }

}
