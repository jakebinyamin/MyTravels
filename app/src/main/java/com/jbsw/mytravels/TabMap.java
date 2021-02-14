package com.jbsw.mytravels;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jbsw.data.GpsDataTable;
import com.jbsw.data.NotesTable;
import com.jbsw.data.TravelMasterTable;
import com.jbsw.utils.PhotoBackgroundLoader;
import com.jbsw.utils.Prefs;
import com.jbsw.utils.Utils;

import org.w3c.dom.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;


// TODO Support view by day
// TODO Add an overlay legend showing what each typ indicates
// TODO Add trip PolyLine
// TODO Add Photos
// TODO Add ability to tap on a photo to view timeline
// TODO Add ability to view Journal
// TODO Put icons inside of GPS icon

public class TabMap extends Fragment   implements OnMapReadyCallback, View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private static final String TAG = "TAGTabMap";
    private static final int FILTER_ACTIVITY = 1;

    private GoogleMap m_Map;
    private MapView m_MapView;
    private View m_ThisWIndow;
    private Spinner m_Spinner;
    private CheckBox m_CBShowJournals;
    private DaysListAdapter m_DaysListAdapter;
    private String m_sFilterDate = null;


    TravelMasterTable.DataRecord m_MDR;

    private ViewPager2 m_EventList;
    private EventsAdapter m_EventsAdapter = null;
    private EventListener m_EventListener = null;
    private boolean m_bInitialising = true;

    private TripActivity m_Parent;
    private float m_GreatestDistance;
    private LatLng m_PtFirst = null, m_PtFurthestFromFirst = null, m_PtLast = null;

//    public TabMap() {
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_ThisWIndow = inflater.inflate(R.layout.fragment_tab_map, container, false);
        Log.d(TAG, "In onCreateView");
        return m_ThisWIndow;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "In onViewCreated processing Initialise");

        InitialiseInThread InitAsync = new InitialiseInThread(view);
        InitAsync.execute();

//        Initialise(view);
        Log.d(TAG, "In onViewCreated Initialise complete");
    }

    private class InitialiseInThread extends AsyncTask<Object,Void,String>
    {
        private View m_View;
        private ProgressDialog dialog;

        public InitialiseInThread(View view)
        {
            m_View = view;
            dialog = new ProgressDialog(getActivity());
        }

        protected void onPreExecute()
        {
            InitialiseInUIThread(m_View);
        }

        @Override
        protected String doInBackground(Object... objects)
        {
            Log.d(TAG, "In thread Initialise");
            Initialise(m_View);
            Log.d(TAG, "Thread Complete Initialise");

            return null;
        }

        @Override
        protected void onPostExecute(String str)
        {
            Log.d(TAG, "onPostExecute Loading MapView");
            m_MapView = m_ThisWIndow.findViewById(R.id.mapView);
            m_MapView.onCreate(null);
            m_MapView.onResume();
            m_MapView.getMapAsync(TabMap.this);
            Log.d(TAG, "onPostExecute Completed Loading MapView");
        }
    }


    private  int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    private void InitialiseInUIThread(View view)
    {
        Log.d(TAG, "InitialiseInUIThread Start");
        m_Parent = (TripActivity) getActivity();
        m_MDR = m_Parent.GetDataRecord();

        m_CBShowJournals = m_ThisWIndow.findViewById(R.id.view_journals);
        m_CBShowJournals.setOnClickListener(this);
        m_CBShowJournals.setChecked(false);

        m_Spinner = m_ThisWIndow.findViewById(R.id.filter);
        m_DaysListAdapter = new DaysListAdapter();
        m_Spinner.setAdapter(m_DaysListAdapter);
        m_Spinner.setSelection(0);
        m_Spinner.setOnItemSelectedListener(this);

        m_EventList = (ViewPager2) view.findViewById(R.id.event_slider2);
        m_EventListener = new EventListener();
        m_EventList.registerOnPageChangeCallback(m_EventListener);

        m_EventsAdapter = new EventsAdapter(getActivity().getSupportFragmentManager(), getLifecycle());

        m_EventList.setAdapter(m_EventsAdapter);
        m_EventList.setPageTransformer( new MarginPageTransformer(pxToDp(40)));
        m_EventList.setOffscreenPageLimit(3);
        m_EventList.setVisibility(View.GONE);
        Log.d(TAG, "InitialiseInUIThread Complete");
    }

    private void Initialise(View view)
    {

        Log.d(TAG, "Before LoadFilterData");
        LoadFilterData();
        Log.d(TAG, "After LoadFilterData");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        if (m_bInitialising)
            return;

        Log.d(TAG, "Processing onResume");
        m_MapView.onResume();

        Prefs prefs = new Prefs(this.getContext());
        if (prefs.HasJournalChanged()) {
            Log.d(TAG, "Refreshing data..");
            m_sFilterDate = null;
            BuildMap();
            m_CBShowJournals.setChecked(false);
            m_EventList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        Log.d(TAG, "In onMapReady");
        MapsInitializer.initialize(getContext());
        m_Map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        m_bInitialising = false;
        Log.d(TAG, "In onMapReady BuildMap started");
        BuildMap();
        Log.d(TAG, "In onMapReady BuildMap complete");
    }


    private void BuildMap()
    {
        m_GreatestDistance = 0;

        m_Map.clear();
//        AddJournalMarkers(m_MDR.Id);
//        AddTripRoute(m_MDR.Id, m_MDR.StartDate);

        BuildMapInBkg MapLoader = new BuildMapInBkg();
        Thread thread = new Thread(MapLoader);
        thread.start();
    }


    private class BuildMapInBkg implements Runnable
    {
        @Override
        public void run() {
            AddJournalMarkers(m_MDR.Id);
            AddTripRoute(m_MDR.Id, m_MDR.StartDate);
        }
    }
    private void AddTripRoute(long Id, String sFromDate)
    {
        GpsDataTable Tab = new GpsDataTable();
        GpsDataTable.DataRecord DR;

        //
        // build the correct Query
        Log.d(TAG, "STarting route qry");
        if (m_sFilterDate == null)
            Tab.QueryAll(Id);
        else
            Tab.QueryForDate(Id, m_sFilterDate);
        Log.d(TAG, "Ending route qry");

        LatLng Pt = null,PtLast = null;
        m_PtFirst = null;
        m_PtFurthestFromFirst = null;
        m_GreatestDistance = 0;
        int ColorList[] = Utils.GetColorList();
        int nClrSize = ColorList.length;
        while ((DR = Tab.GetNextRecord()) != null)
        {
            PtLast = Pt;
            Pt =  new LatLng( DR.nLatitude, DR.nLongitude);
            if (m_PtFirst == null)
                m_PtFirst = Pt;
            float [] results = new float[3];
            if (PtLast != null) {
                Location.distanceBetween(PtLast.latitude, PtLast.longitude, Pt.latitude,  Pt.longitude, results);
                float [] res2 = new float[3];
                Location.distanceBetween(m_PtFirst.latitude, m_PtFirst.longitude, Pt.latitude,  Pt.longitude, res2);
                if (res2[0] > m_GreatestDistance) {
                    m_GreatestDistance = res2[0];
                    m_PtFurthestFromFirst = Pt;
                }
            }
            if (PtLast != null && results[0] > 50.0)
            {
                long nDay = Utils.CalculateDays(sFromDate, DR.Date);
                int color = ColorList[(int)nDay % nClrSize];
                int ActualColor = ContextCompat.getColor(m_ThisWIndow.getContext(), color);

                final LatLng InnerPtLast = PtLast;
                final LatLng InnerPt = Pt;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       Polyline polyline1 = m_Map.addPolyline(new PolylineOptions().clickable(false).add(InnerPtLast, InnerPt).color(ActualColor)); //Color.RED
                    }
                });
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Zoom(m_PtFirst, m_PtFurthestFromFirst);
            }
         });
    }

    private void Zoom(LatLng PtFirst, LatLng PtLast)
    {
        if (PtFirst == null || PtLast == null)
            return;

        final LatLngBounds bounds = new LatLngBounds.Builder().include(PtFirst).include(PtLast).build();
        m_Map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300));
    }

//    private void Zoom(LatLng Pt, LatLng PtFirst)
//    {
//        if (Pt == null && m_PtLast == null)
//            return;
//
//        if (Pt == null)
//            Pt = m_PtLast;
//
//        Log.d(TAG, "Greatest Distance: " + m_GreatestDistance + " Pt: " + Pt + " PtFirst: " + PtFirst);
//        float zoomLevel = 0.0f;
//        if (m_GreatestDistance < 700)
//            zoomLevel = 16.5f;
//        else if (m_GreatestDistance < 2000)
//            zoomLevel = 14.5f;
//        else if (m_GreatestDistance < 10000)
//            zoomLevel = 13.0f;
//        else if (m_GreatestDistance < 30000)
//            zoomLevel = 12.0f;
//        else if (m_GreatestDistance < 40000)
//            zoomLevel = 10.0f;
//        else if (m_GreatestDistance < 60000)
//            zoomLevel = 9.5f;
//        else if (m_GreatestDistance < 100000)
//            zoomLevel = 9.0f;
//        else if (m_GreatestDistance < 200000)
//            zoomLevel = 8.5f;
//        else if (m_GreatestDistance < 300000)
//            zoomLevel = 7.5f;
//        else if (m_GreatestDistance < 400000)
//            zoomLevel = 6.5f;
//        else
//            zoomLevel = 4.0f;
//
//        final float Innerzoom = zoomLevel;
//        final LatLng InnerPt = Pt;
//
//        if (PtFirst == null) {
//            m_Map.animateCamera(CameraUpdateFactory.newLatLngZoom(InnerPt, Innerzoom));
//        }
//        else {
//            final LatLngBounds bounds = new LatLngBounds.Builder().include(InnerPt).include(PtFirst).build();
//            m_Map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
//        }
////        getActivity().runOnUiThread(
////        new Runnable() {
////            @Override
////            public void run() {
////                if (PtFirst == null) {
////                    m_Map.animateCamera(CameraUpdateFactory.newLatLngZoom(InnerPt, Innerzoom));
////                }
////                else {
////                    final LatLngBounds bounds = new LatLngBounds.Builder().include(InnerPt).include(new LatLng(minLat, minLon)).build();
////                    m_Map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
////                }
////
////            }
////        });
//
//        m_PtLast = Pt;
//    }

    private void AddJournalMarkers(long Id)
    {
        NotesTable Tab = new NotesTable();

        Tab.QueryAllForMap(Id, m_sFilterDate);
        NotesTable.DataRecord JDR;

        LatLng Pt = null;
        while ((JDR = Tab.GetNextRecord()) != null)
        {
//            if (JDR.nLatitude != -1 || JDR.nLongitude != -1)
            {
                Pt = new LatLng(JDR.nLatitude, JDR.nLongitude);
                final int IconList[] = Utils.GetMapList();
                Drawable d = getResources().getDrawable(IconList[JDR.nType], getContext().getTheme());
                Bitmap bm = Utils.drawableToBitmap(d);
                Bitmap icon = Bitmap.createScaledBitmap(bm, 100, 100, true);

                final LatLng finalPt = Pt;
                final String sTitle = JDR.sTitle;
                getActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        m_Map.addMarker(new MarkerOptions().position(finalPt).title(sTitle).icon(BitmapDescriptorFactory.fromBitmap(icon)));
                    }
                });
            }
        }
    }

    //
    // Methods for when an item selected from, spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (m_bInitialising)
            return;

        if (position == 0) {
            m_sFilterDate = null;
        }
        else {
            FilterDataPkt Data = m_DataList.get(position-1);
            m_sFilterDate = String.format("%d-%02d-%02d", Data.m_Date.getYear(), Data.m_Date.getMonthValue(), Data.m_Date.getDayOfMonth());
        }

        if (m_CBShowJournals.isChecked())
        {
            ResetEventList();
        }
        BuildMap();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private void ResetEventList()
    {
        m_EventList.setVisibility(View.GONE);
        m_EventsAdapter = null;
        m_EventsAdapter = new EventsAdapter(getActivity().getSupportFragmentManager(), getLifecycle());
        m_EventList.setAdapter(m_EventsAdapter);
        m_EventsAdapter.Refresh();
        m_EventList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v)
    {
//        if (v == m_BtnFilter) {
//            if (m_BtnFilter.isChecked()) {
//                Intent i = new Intent(m_ThisWIndow.getContext(), DlgFilter.class);
//                i.putExtra(DlgFilter.PARAM_ID, m_MDR.Id);
//                i.putExtra(DlgFilter.PARAM_FROM_DATE, m_MDR.StartDate);
//                startActivityForResult(i, FILTER_ACTIVITY);
//            }
//            else {
//                m_TxtFilterDate.setVisibility(View.GONE);
//                BuildMap(null);
//            }
//        }

        if (v == m_CBShowJournals)
        {
            if (m_CBShowJournals.isChecked())
            {
                Log.d(TAG, "Checkbox checked - b4 ResetEventList");
                ResetEventList();
                Log.d(TAG, "Checkbox checked - after ResetEventList");
            }
            else {
                Log.d(TAG, "Checkbox off");
                m_EventList.setVisibility(View.GONE);
                Zoom(m_PtFirst, m_PtFurthestFromFirst);
            }

        }
    }

    protected class FilterDataPkt
    {
        public LocalDate m_Date;
        public long m_nDay;

        FilterDataPkt(LocalDate Date, long nDay)
        {
            m_Date = Date;
            m_nDay = nDay;
        }
    }

    private ArrayList <TabMap.FilterDataPkt> m_DataList = new ArrayList<>();

    private void LoadFilterData()
    {
        if (m_MDR.Id < 0)
            return;
        GpsDataTable Tab = new GpsDataTable();
        GpsDataTable.DataRecord DR;

        m_DataList.clear();
        Tab.QueryAll(m_MDR.Id);
        TabMap.FilterDataPkt Last = null;
        while ((DR = Tab.GetNextRecord()) != null)
        {
            LocalDate date = Utils.GetDateFromString(DR.Date);
            long nDays = Utils.CalculateDays(m_MDR.StartDate, DR.Date);
            if (Last == null) {
                Last = new TabMap.FilterDataPkt(date, nDays);
                m_DataList.add(Last);
            }
            else {
                if (Last.m_nDay != nDays) {
                    Last = new TabMap.FilterDataPkt(date, nDays);
                    m_DataList.add(Last);
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Filter spinner adapter

    private class DaysListAdapter extends BaseAdapter implements SpinnerAdapter
    {
        @Override
        public int getCount() {
            return m_DataList.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0) {
                return null;
            }
            return m_DataList.get(position-1);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent, false);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent, true);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent, boolean bFullTransparency)
        {
            if (convertView == null) {
                convertView = LayoutInflater.from(m_ThisWIndow.getContext()).inflate(R.layout.daylist_row, parent, false);
            }

            String sOut = getResources().getString(R.string.all_trip);
            View Line = convertView.findViewById(R.id.legend);

            if (position > 0)
            {
                TabMap.FilterDataPkt Data = (TabMap.FilterDataPkt) getItem(position);
                DayOfWeek nDow = Data.m_Date.getDayOfWeek();
                String sDow = nDow.getDisplayName(TextStyle.SHORT, Locale.getDefault());
                Month nMonth = Data.m_Date.getMonth();
                String sMonth = nMonth.getDisplayName(TextStyle.SHORT, Locale.getDefault());
                sOut = String.format("Day %d: %s %s %d", Data.m_nDay, sDow, sMonth, Data.m_Date.getDayOfMonth());

                Line.setVisibility(View.VISIBLE);
                int ColorList[] = Utils.GetColorList();
                int nClrSize = ColorList.length;
                int color = ColorList[(int)Data.m_nDay % nClrSize];
                int ActualColor = ContextCompat.getColor(m_ThisWIndow.getContext(), color);
                Line.setBackgroundColor(ActualColor);
            }
            else
            {
                sOut = getResources().getString(R.string.all_trip);
                Line.setVisibility(View.GONE);
            }

            TextView v = (TextView) convertView.findViewById(R.id.listitem);
            v.setText(sOut);
            if (bFullTransparency) {
                convertView.setBackgroundResource(R.color.fulltransparent);
                v.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.white));
            }

            return convertView;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // OnPageChangeListener events
    private class EventListener extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            NotesTable.DataRecord DR = m_EventsAdapter.GetRecordAtPosition(position);
            if (DR == null)
                return;
            LatLng Pt = new LatLng(DR.nLatitude, DR.nLongitude);
            float zoomLevel = 19.0f;
            m_Map.animateCamera(CameraUpdateFactory.newLatLngZoom(Pt, zoomLevel));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Events Adapter
    private class EventsAdapter extends FragmentStateAdapter
    {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private NotesTable m_NotesTable;

        public EventsAdapter(FragmentManager fm, Lifecycle l) {
            super(fm, l);
            m_NotesTable = new NotesTable();
        }



        public void Refresh()
        {
            fragmentList.clear();
            m_NotesTable.QueryAllForMap(m_MDR.Id, m_sFilterDate);
            Log.d(TAG, "Clearing out and doing a new Query..");
            NotesTable.DataRecord JDR;
            int nItem = 0;
            while ((JDR = m_NotesTable.GetNextRecord()) != null)
            {
                Fragment Frag = EventCardFragment.newInstance(m_NotesTable, m_MDR.StartDate, nItem++);
                fragmentList.add(Frag);
            }
            notifyDataSetChanged();
            m_EventList.setCurrentItem(0);
            m_EventListener.onPageSelected(0);
        }

        public NotesTable.DataRecord GetRecordAtPosition(int position)
        {
            return m_NotesTable.GetDataAtPosition(position);
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////

}
