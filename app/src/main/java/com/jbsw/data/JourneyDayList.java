package com.jbsw.data;

import com.jbsw.mytravels.TabMap;
import com.jbsw.utils.Utils;

import java.time.LocalDate;
import java.util.ArrayList;

public class JourneyDayList
{
    private TravelMasterTable.DataRecord m_DR;

    public JourneyDayList(TravelMasterTable.DataRecord dr)
    {
        m_DR = dr;
    }

    public class JourneyDayItem
    {
        public LocalDate m_Date;
        public long m_nDay;

        JourneyDayItem(LocalDate Date, long nDay)
        {
            m_Date = Date;
            m_nDay = nDay;
        }
    }

    private ArrayList<JourneyDayItem> m_DataList = new ArrayList<>();

    public ArrayList<JourneyDayItem> GetList()
    {
        return m_DataList;
    }

    public JourneyDayItem Get(int id)
    {
        if (m_DataList == null)
            return null;

        return m_DataList.get(id);
    }

    public int GetCount()
    {
        if (m_DataList == null)
            return 0;

        return m_DataList.size();
    }

    public void LoadData()
    {
        if (m_DR.Id < 0)
            return;

        GpsDataTable Tab = new GpsDataTable();
        GpsDataTable.DataRecord DR;

        m_DataList.clear();
        Tab.QueryAll(m_DR.Id);
        JourneyDayItem Last = null;
        while ((DR = Tab.GetNextRecord()) != null)
        {
            LocalDate date = Utils.GetDateFromString(DR.Date);
            long nDays = Utils.CalculateDays(m_DR.StartDate, DR.Date);
            if (Last == null) {
                Last = new JourneyDayItem(date, nDays);
                m_DataList.add(Last);
            }
            else {
                if (Last.m_nDay != nDays) {
                    Last = new JourneyDayItem(date, nDays);
                    m_DataList.add(Last);
                }
            }
        }
    }

}
