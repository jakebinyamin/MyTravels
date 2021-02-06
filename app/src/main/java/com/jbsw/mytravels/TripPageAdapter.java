package com.jbsw.mytravels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TripPageAdapter extends FragmentPagerAdapter
{
    private static final String TAG = "TAGTripPageAdapter";

    public static final int TAB_GENERAL = 0;
    public static final int TAB_JOURNAL = 1;
    public static final int TAB_MAP = 2;

    private TabGeneral  m_TabGeneral = null;
    private TabJournal  m_TabJournal = null;
    private TabMap  m_TabMap = null;

    private int m_nTabs;

    public TripPageAdapter(@NonNull FragmentManager fm, int nTabs)
    {
        super(fm);
        m_nTabs = nTabs;
        Log.d(TAG, "in TripPageAdapter Constructor, tabs: " + m_nTabs);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == TAB_GENERAL) {
            Log.d(TAG, "TAB_GENERAL");
            m_TabGeneral = new TabGeneral();
            return m_TabGeneral;
        }
        if (position == TAB_JOURNAL) {
            Log.d(TAG, "TAB_JORNAL");
            m_TabJournal = new TabJournal();
            return m_TabJournal;
        }
        if (position == TAB_MAP) {
            Log.d(TAG, "TAB_MAP");
            m_TabMap = new TabMap();
            return m_TabMap;
        }
        return null;
    }

    public Fragment GetFragment(int position)
    {
        if (position == TAB_GENERAL)
            return m_TabGeneral;
        if (position == TAB_JOURNAL)
            return m_TabJournal;
        if (position == TAB_MAP) {
            Log.d(TAG, "Launching Map..");
            return m_TabMap;
        }

        return null;
    }

    @Override
    public int getCount() {
        return m_nTabs;
    }

    @Override
    public int getItemPosition(@NonNull Object object)
    {
        return POSITION_NONE;
//        return super.getItemPosition(object);
    }
}
