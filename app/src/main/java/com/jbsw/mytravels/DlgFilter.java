package com.jbsw.mytravels;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jbsw.data.GpsDataTable;
import com.jbsw.utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class DlgFilter extends Activity implements ListView.OnItemClickListener
{

    public static final String PARAM_ID         = "_id";
    public static final String PARAM_FROM_DATE  = "_FromDate";
    public static final String PARAM_FILTER_DATE  = "_FilterDate";

    public static final int RESULT_OK = 1;
    public static final int RESULT_NONE = 2;

    private long m_Id;
    private String m_sFromDate;

    private DaysListAdapter m_ListAdapter;


    protected class DataPkt
    {
        public LocalDate m_Date;
        public long m_nDay;

        DataPkt(LocalDate Date, long nDay)
        {
            m_Date = Date;
            m_nDay = nDay;
        }
    }

    private ArrayList <DataPkt> m_DataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlg_filter);

        m_Id = getIntent().getLongExtra(PARAM_ID, -1);
        m_sFromDate = getIntent().getStringExtra(PARAM_FROM_DATE);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ListView List = findViewById(R.id.daylist);
        m_ListAdapter = new DaysListAdapter();
        List.setAdapter(m_ListAdapter);
        List.setOnItemClickListener(this);

        SetupScreenSize();
        LoadData();
        m_ListAdapter.notifyDataSetChanged();
    }

    private void SetupScreenSize()
    {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int nWidth = dm.widthPixels;
        int nHeight = dm.heightPixels;

        getWindow().setLayout((int)(nWidth*.6), (int) (nHeight*.4) );

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.y = 300;
        params.x = -100;
        getWindow().setAttributes(params);
    }

    private void LoadData()
    {
        if (m_Id < 0)
            return;
        GpsDataTable Tab = new GpsDataTable();
        GpsDataTable.DataRecord DR;

        Tab.QueryAll(m_Id);
        DataPkt Last = null;
        while ((DR = Tab.GetNextRecord()) != null)
        {
            LocalDate date = Utils.GetDateFromString(DR.Date);
            long nDays = Utils.CalculateDays(m_sFromDate, DR.Date);
            if (Last == null) {
                Last = new DataPkt(date, nDays);
                m_DataList.add(Last);
            }
            else {
                if (Last.m_nDay != nDays) {
                    Last = new DataPkt(date, nDays);
                    m_DataList.add(Last);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        DataPkt Data = (DataPkt) m_ListAdapter.getItem(position);
        String sOutDate = String.format("%d-%02d-%02d", Data.m_Date.getYear(), Data.m_Date.getMonthValue(), Data.m_Date.getDayOfMonth());
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PARAM_FILTER_DATE, sOutDate);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private class DaysListAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return m_DataList.size();
        }

        @Override
        public Object getItem(int position) {
            return m_DataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = LayoutInflater.from(DlgFilter.this).inflate(R.layout.daylist_row, parent, false);
            }

            DataPkt Data = (DataPkt) getItem(position);
            DayOfWeek nDow = Data.m_Date.getDayOfWeek();
            String sDow = nDow.getDisplayName(TextStyle.SHORT, Locale.getDefault());;
            Month nMonth = Data.m_Date.getMonth();
            String sMonth = nMonth.getDisplayName(TextStyle.SHORT, Locale.getDefault());

            String sOut = String.format("Day %d: %s %s %d", Data.m_nDay, sDow, sMonth, Data.m_Date.getDayOfMonth());

            TextView v = (TextView) convertView.findViewById(R.id.listitem);
            v.setText(sOut);

            return convertView;
        }
    }
}