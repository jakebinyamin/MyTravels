package com.jbsw.mytravels;

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
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;


// TODO Support view by day
// TODO Add an overlay legend showing what each typ indicates
// TODO Add trip PolyLine
// TODO Add Photos
// TODO Add ability to tap on a photo to view timeline
// TODO Add ability to view Journal
// TODO Put icons inside of GPS icon

public class TabMap extends Fragment   implements OnMapReadyCallback, View.OnClickListener
{
    private static final String TAG = "TAGTabMap";
    private static final int FILTER_ACTIVITY = 1;

    private GoogleMap m_Map;
    private MapView m_MapView;
    private View m_ThisWIndow;

    TravelMasterTable.DataRecord m_MDR;

    private ToggleButton m_BtnFilter;
    private ToggleButton m_BtnEvents;
    private ToggleButton m_BtnLegend;
    private TextView m_TxtFilterDate;
    private ViewPager2 m_EventList;
    private EventsAdapter m_EventsAdapter = null;
    private EventListener m_EventListener = null;

    private TripActivity m_Parent;
    private float m_GreatestDistance;
    private LatLng m_PtFirst, m_PtLast = null;
    private boolean m_bInitialised = false;

    public TabMap() {
        Log.d(TAG, "Constructor...");
    }

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
        Log.d(TAG, "In onViewCreated");


        Initialise(view);

    }

    private  int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    private void Initialise(View view)
    {
        m_Parent = (TripActivity) getActivity();
        m_MDR = m_Parent.GetDataRecord();

        m_BtnFilter = (ToggleButton) view.findViewById(R.id.filter);
        m_BtnFilter.setChecked(false);
        m_BtnFilter.setOnClickListener(this);

        m_BtnEvents = (ToggleButton) view.findViewById(R.id.events);
        m_BtnEvents.setChecked(false);
        m_BtnEvents.setOnClickListener(this);

        m_BtnLegend = (ToggleButton) view.findViewById(R.id.legend);
        m_BtnLegend.setOnClickListener(this);
        m_BtnLegend.setChecked(false);

        m_TxtFilterDate = (TextView) view.findViewById(R.id.filter_description);
        m_TxtFilterDate.setVisibility(View.GONE);

        m_EventList = (ViewPager2) view.findViewById(R.id.event_slider2);
        m_EventListener = new EventListener();
        m_EventList.registerOnPageChangeCallback(m_EventListener);

        //
        // Setup Photo Adapter
//        if (!m_bInitialised)
//        m_EventsAdapter = new EventsAdapter(this /*getActivity().getSupportFragmentManager()*/);
        m_EventsAdapter = new EventsAdapter(getActivity().getSupportFragmentManager(), getLifecycle());

        m_EventList.setAdapter(m_EventsAdapter);
        m_EventList.setPageTransformer( new MarginPageTransformer(pxToDp(40)));
        m_EventList.setOffscreenPageLimit(3);


        m_MapView = m_ThisWIndow.findViewById(R.id.mapView);
        if (m_MapView != null) {
            m_MapView.onCreate(null);
            m_MapView.onResume();
            m_MapView.getMapAsync(this);
        }

        m_bInitialised = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        m_BtnEvents.setChecked(false);
        m_BtnFilter.setChecked(false);
        m_MapView.onResume();

        Prefs prefs = new Prefs(this.getContext());
        if (prefs.HasJournalChanged()) {
            Log.d(TAG, "Refreshing data..");
            BuildMap(null);
        }
        Log.d(TAG, "onResume");
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        MapsInitializer.initialize(getContext());
        m_Map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        BuildMap(null);
    }



     @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        m_BtnFilter.setChecked(false);
        if (requestCode == FILTER_ACTIVITY && resultCode == DlgFilter.RESULT_OK) {
            String sDate = data.getStringExtra(DlgFilter.PARAM_FILTER_DATE);
            BuildMap(sDate);
            m_BtnFilter.setChecked(true);
            m_TxtFilterDate.setVisibility(View.VISIBLE);
            long nDays = Utils.CalculateDays(m_MDR.StartDate, sDate);
            sDate = Utils.GetReadableStringDate(sDate);
            if (sDate != null) {
                String s = String.format(m_ThisWIndow.getResources().getString(R.string.view_activities_for), nDays, sDate);
                m_TxtFilterDate.setText(s);
            }
        }
    }

    private void BuildMap(String sFilterDate)
    {
        m_GreatestDistance = 0;
        m_PtFirst = null;

        m_Map.clear();
        AddJournalMarkers(m_MDR.Id);
        AddTripRoute(m_MDR.Id, m_MDR.StartDate, sFilterDate);

//        BuildMapInBkg MapLoader = new BuildMapInBkg(sFilterDate);
//        Thread thread = new Thread(MapLoader);
//        thread.start();
    }

    private class BuildMapInBkg implements Runnable
    {
        String m_sFilterDate;
        public BuildMapInBkg(String sFilterDate)
        {
            m_sFilterDate = sFilterDate;
        }

        @Override
        public void run() {
            getActivity().runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    m_Map.clear();
                }
            });

            m_GreatestDistance = 0;
            m_PtFirst = null;

            AddJournalMarkers(m_MDR.Id);
            AddTripRoute(m_MDR.Id, m_MDR.StartDate, m_sFilterDate);
        }
    }
    private void AddTripRoute(long Id, String sFromDate, String sFilterDate)
    {
        GpsDataTable Tab = new GpsDataTable();
        GpsDataTable.DataRecord DR;

        //
        // build the correct Query
        if (sFilterDate == null)
            Tab.QueryAll(Id);
        else
            Tab.QueryForDate(Id, sFilterDate);

        LatLng Pt = null,PtLast = null;
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
                }
            }
            if (PtLast != null && results[0] > 50.0)
            {
                long nDay = Utils.CalculateDays(sFromDate, DR.Date);
                int color = ColorList[(int)nDay % nClrSize];
                int ActualColor = ContextCompat.getColor(m_ThisWIndow.getContext(), color);

                final LatLng InnerPtLast = PtLast;
                final LatLng InnerPt = Pt;
                getActivity().runOnUiThread(
                new Runnable() {
                   public void run() {
                       Polyline polyline1 = m_Map.addPolyline(new PolylineOptions().clickable(false).add(InnerPtLast, InnerPt).color(ActualColor)); //Color.RED
                   }
                });
            }
        }

        Zoom(Pt);
    }

    private void Zoom(LatLng Pt)
    {
        if (Pt == null && m_PtLast == null)
            return;

        if (Pt == null)
            Pt = m_PtLast;

        float zoomLevel = 0.0f;
        if (m_GreatestDistance < 700)
            zoomLevel = 17.0f;
        else if (m_GreatestDistance < 2000)
            zoomLevel = 15.0f;
        else if (m_GreatestDistance < 10000)
            zoomLevel = 13.0f;
        else if (m_GreatestDistance < 30000)
            zoomLevel = 12.0f;
        else if (m_GreatestDistance < 40000)
            zoomLevel = 10.0f;
        else if (m_GreatestDistance < 60000)
            zoomLevel = 9.5f;
        else if (m_GreatestDistance < 100000)
            zoomLevel = 9.0f;
        else if (m_GreatestDistance < 200000)
            zoomLevel = 8.5f;
        else if (m_GreatestDistance < 300000)
            zoomLevel = 7.5f;
        else if (m_GreatestDistance < 400000)
            zoomLevel = 6.5f;
        else
             zoomLevel = 4.0f;

        final float Innerzoom = zoomLevel;
        final LatLng InnerPt = Pt;
        getActivity().runOnUiThread(
        new Runnable() {
            @Override
            public void run() {
                m_Map.animateCamera(CameraUpdateFactory.newLatLngZoom(InnerPt, Innerzoom));
            }
        });

        m_PtLast = Pt;
    }

    private void AddJournalMarkers(long Id)
    {
        NotesTable Tab = new NotesTable();

        Tab.QueryAllForMap(Id);
        NotesTable.DataRecord JDR;

        LatLng Pt = null;
        while ((JDR = Tab.GetNextRecord()) != null)
        {
            if (JDR.nLatitude != -1 || JDR.nLongitude != -1)
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


    @Override
    public void onClick(View v)
    {
        if (v == m_BtnFilter) {
            if (m_BtnFilter.isChecked()) {
                Intent i = new Intent(m_ThisWIndow.getContext(), DlgFilter.class);
                i.putExtra(DlgFilter.PARAM_ID, m_MDR.Id);
                i.putExtra(DlgFilter.PARAM_FROM_DATE, m_MDR.StartDate);
                startActivityForResult(i, FILTER_ACTIVITY);
            }
            else {
                m_TxtFilterDate.setVisibility(View.GONE);
                BuildMap(null);
            }
        }

        if (v == m_BtnEvents)
        {
            if (m_BtnEvents.isChecked())
            {
                m_EventsAdapter = null;
                m_EventsAdapter = new EventsAdapter(getActivity().getSupportFragmentManager(), getLifecycle());
                m_EventList.setAdapter(m_EventsAdapter);
                m_EventsAdapter.Refresh();
                m_EventList.setVisibility(View.VISIBLE);
            }
            else {
                m_EventList.setVisibility(View.GONE);
                Zoom(null);
            }

        }

        if (v == m_BtnLegend)
        {
            if (m_BtnLegend.isChecked())
            {
            }

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
            m_NotesTable.QueryAllForMap(m_MDR.Id);
            Log.d(TAG, "Clearing out and doing a new Query..");
            NotesTable.DataRecord JDR;
            int nItem = 0;
            while ((JDR = m_NotesTable.GetNextRecord()) != null)
            {
                Log.d(TAG, "Adding Journal Record: " + JDR.sTitle);
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
