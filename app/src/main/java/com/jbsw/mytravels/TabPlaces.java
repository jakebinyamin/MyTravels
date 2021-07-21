package com.jbsw.mytravels;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.jbsw.data.GpsDataTable;
import com.jbsw.data.JourneyDayList;
import com.jbsw.data.TravelMasterTable;
import com.jbsw.utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import static java.lang.Float.MAX_VALUE;

public class TabPlaces extends Fragment implements PhotoListViewAdapter.PhotoItemClickListener
{

    private static final String TAG = "TAGTabPlaces";

    enum PlaceStatus { PLACE_STATIONARY, PLACE_MOVING}
    enum MovingType { MOV_NONE, MOV_WALK, MOV_DRIVE, MOV_FLIGHT}

    View m_ThisWIndow;
    private ListPlacesAdapter m_ListAdapter;
    private ListView m_PlacesList;

    protected ArrayList<String> m_PhotoList = null;
    protected PhotoListViewAdapter m_PhotoListAdapter;
    RecyclerView m_PhotoRecyclerView;

    private TravelMasterTable.DataRecord m_DR;
    private TripActivity m_Parent = null;
    private static final int MINUTES_IN_ONEPLACE = 3;
    private DayListAdapter m_DLA;
    private Semaphore m_BuildDataSem = new Semaphore(1);

    private class DataLine
    {
        public String m_sLine1;
        public String m_sLine2;
        public String m_sTime;
        public PlaceStatus m_Status;
        public MovingType m_MovType;

       DataLine(String sL1, String sL2, String sTime, PlaceStatus stat, MovingType type)
        {
            m_sLine1 = sL1;
            m_sLine2 = sL2;
            m_Status = stat;
            m_sTime = sTime;
            m_MovType = type;
        }
    }

    ArrayList<DataLine> m_DataList = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_ThisWIndow = inflater.inflate(R.layout.fragment_tab_places, container, false);
        return m_ThisWIndow;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        m_ThisWIndow = view;
        m_Parent = (TripActivity) getActivity();
        m_DR = m_Parent.GetDataRecord();

        try {
            m_PlacesList = (ListView) m_ThisWIndow.findViewById(R.id.places_list);
            m_ListAdapter = new ListPlacesAdapter();
            m_PlacesList.setAdapter(m_ListAdapter);
            RecyclerView recyclerView = m_ThisWIndow.findViewById(R.id.day_list);
            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(horizontalLayoutManager);
            m_DLA = new DayListAdapter();
            recyclerView.setAdapter(m_DLA);

            m_PhotoRecyclerView = m_ThisWIndow.findViewById(R.id.photo_list);
            LinearLayoutManager horizontalLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            m_PhotoRecyclerView.setLayoutManager(horizontalLayoutManager2);
            m_PhotoList = new ArrayList<String>();
            m_PhotoListAdapter = new PhotoListViewAdapter(m_PhotoList);
            m_PhotoListAdapter.SetSemaphore(m_BuildDataSem);
            m_PhotoListAdapter.setPhotoClickListener(this);
            m_PhotoRecyclerView.setAdapter(m_PhotoListAdapter);

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    @Override
    public void onPhotoItemClick(View view, int position)
    {
        Intent photoIntent = new Intent(m_ThisWIndow.getContext(), PhotoViewer.class);
        photoIntent.putStringArrayListExtra(PhotoViewer.IntentData, m_PhotoList);
        photoIntent.putExtra(PhotoViewer.IntentPosn, position);

        startActivity(photoIntent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // Initialisation thread
    private class LoadDataInThread extends AsyncTask<Object, Void, String>
    {
        private String m_sQueryDate;
        LocalDate m_Date;
        private PlaceStatus m_Pos;

        public LoadDataInThread(LocalDate Date)
        {
            m_Date = Date;
            m_sQueryDate = String.format("%d-%02d-%02d", Date.getYear(), Date.getMonthValue(), Date.getDayOfMonth());
            Log.d(TAG, "Querying date: " + m_sQueryDate);
        }

        @Override
        protected void onPreExecute()
        {
            Log.d(TAG, "Starting onPreExecute");
            super.onPreExecute();
            if (m_DataList == null)
                m_DataList = new ArrayList<>();
            else
                m_DataList.clear();
            m_ListAdapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(Object... objects)
        {
            GpsDataTable gpsdata = new GpsDataTable();
            gpsdata.QueryForDate(m_DR.Id, m_sQueryDate);
            GpsDataTable.DataRecord gpsDR;
            String dCurr = null, dPrev = null, dStart = null;
            LatLng posCur = null, posPrev = null, posPrint = null, posStart = null;
            float fDistTravelled = 0;
            m_Pos = PlaceStatus.PLACE_STATIONARY;
            int nCountEntries = 0;
            while ((gpsDR = gpsdata.GetNextRecord()) != null) {
                try {
                    //
                    // Collect data..
                    if (dCurr != null)
                        dPrev = dCurr;
                    dCurr = gpsDR.Date;
                    if (dPrev == null)
                        dStart = dCurr;

                    if (posCur != null)
                        posPrev = posCur;
                    posCur = new LatLng(gpsDR.nLatitude, gpsDR.nLongitude);
                    if (posPrev == null)
                        posPrint = posCur;

                    //
                    // If first then no checking...
                    if (posPrev == null && dPrev == null)
                        continue;

                    float nDist = DistanceBetween(posCur, posPrev);
                    long TimeDiff = TimeDiff(dCurr, dPrev);
                    float nSpeed = MAX_VALUE;
                    if (TimeDiff > 0)
                        nSpeed = nDist / TimeDiff;

//                    Log.d("JAKE", "Distance between: " + nDist + " M,  Time Between: " + TimeDiff + " minutes - Meters/min: " + nDist/TimeDiff);
//                    DebugLocation(posCur, gpsDR.Date);

                    //
                    // Process STATIONARY..
                    if (m_Pos == PlaceStatus.PLACE_STATIONARY) {
                        //
                        // check for changing between stationary and moving
                        if ((nDist > 300 && nSpeed > 5) || (TimeDiff == 0 && nDist > 50)) {
                            String sTime = (nSpeed < 17 && TimeDiff > 15) ? dCurr : dPrev; // account for low gps readings when in the same place..
                            AddNewPlace(posPrint, dStart, sTime);
                            dStart = sTime;
                            posStart = posPrev;
                            fDistTravelled = nDist;
                            m_Pos = PlaceStatus.PLACE_MOVING;
                        } else {
                            nCountEntries++;
                            if (nCountEntries < 2)
                                posPrint = posCur;
                        }
                    }

                    //
                    // Process MOVING
                    else {
                        //
                        // Check if we need to change
                        if (TimeDiff > 10  || nSpeed < 12) {
                            posPrint = posCur;
                            AddMoving(dStart, dPrev, posStart, posPrev, fDistTravelled);
                            m_Pos = PlaceStatus.PLACE_STATIONARY;
                            dStart = dPrev;
                            nCountEntries = 0;
                        }
                        else
                            fDistTravelled += nDist;

                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.getMessage());
                }

            }

            //
            // If in the same spot, then doc it
            if (m_Pos == PlaceStatus.PLACE_STATIONARY)
                AddNewPlace(posPrint, dStart, dPrev);

            GetPhotosForDate();
            Log.d(TAG, "Ending thread");
            return null;
        }

        @Override
        protected void onPostExecute(String str)
        {
            Log.d(TAG, "Starting onPostExecute");
            m_ListAdapter.notifyDataSetChanged();
            m_PhotoListAdapter.UpdateList(m_PhotoList);
            m_PhotoListAdapter.notifyDataSetChanged();
            if (m_PhotoList.isEmpty())
                m_PhotoRecyclerView.setVisibility(View.GONE);
            else
                m_PhotoRecyclerView.setVisibility(View.VISIBLE);

            m_BuildDataSem.release();
        }

        private void DebugLocation(LatLng loc, String sDate)
        {
            try {
                Locale aLocale = new Locale.Builder().setLanguage("en").setScript("Latn").setRegion("RS").build();
                Geocoder geocoder = new Geocoder(m_ThisWIndow.getContext(), aLocale /*Locale.ENGLISH*//*Locale.getDefault()*/);
                List<Address> addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1);
                Address obj = addresses.get(0);
                String sObj = obj.toString();
//                Log.d(TAG, "Address: " + sObj);
                Log.d("JAKE", "Start : "+ sDate + " ==>>  Address: " + obj.getAddressLine(0));
            } catch (Exception e) {
                Log.e(TAG, "Exception getting location data: " + e.getMessage());
            }

        }

        private void AddNewPlace(LatLng loc, String sStartTime, String sEndTime)
        {
            try {
                String sTime1 = Utils.GetTimeFromString(sStartTime);
                String sTime2 = Utils.GetTimeFromString(sEndTime);
                String sTime = sTime1;
                if (!sTime1.equals(sTime2))
                    sTime = String.format("%s - %s", sTime1, sTime2 );

                Geocoder geocoder = new Geocoder(m_ThisWIndow.getContext(), Locale.US/*Locale.getDefault()*/);
                List<Address> addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1);
                Address obj = addresses.get(0);
                String sLine1;
                if (obj.getThoroughfare() == null || obj.getFeatureName() == null)
                    sLine1 = obj.getAddressLine(0);
                else {
                    sLine1 = obj.getThoroughfare();
                    if (!obj.getThoroughfare().equals(obj.getFeatureName()))
                        sLine1 += ", " + obj.getFeatureName();
                }
                String sLine2 = obj.getLocality();
//                Log.d(TAG, "Address data: " + obj.toString());
                DataLine Dat = new DataLine(sLine1, sLine2, sTime, PlaceStatus.PLACE_STATIONARY, MovingType.MOV_NONE);
                m_DataList.add(Dat);

                /* Log.d(TAG, "Start : "+ sStartTime + ", End: "+ sEndTime + " ==>>  Address: " +obj.getAddressLine(0)); */
            } catch (Exception e) {
                Log.e(TAG, "Exception getting location data: " + e.getMessage());
            }
        }

        private void AddMoving(String sStart, String sEnd, LatLng posStart, LatLng posEnd, float fDist)
        {
            long TimeDiff = TimeDiff(sStart, sEnd);
            String sTimeType;
            float Time = TimeDiff;

            MovingType type = MovingType.MOV_DRIVE;
            String sMovingType = GetString(R.string.driving);
            if (fDist / TimeDiff < 90) {
                sMovingType = GetString(R.string.walking);
                type = MovingType.MOV_WALK;
            }
            if (fDist / TimeDiff > 10000) {
                sMovingType = GetString(R.string.flight);
                type = MovingType.MOV_FLIGHT;
            }

            if (TimeDiff < 60)
                sTimeType = GetString(R.string.minutes);
            else {
                Time = TimeDiff / 60;
                Time = (float) (Math.round(Time*10)/10D);
                sTimeType = GetString(R.string.hours);
            }

            String sTime = String.format("%.1f %s", Time, sTimeType);

            String sDistType;
            if (fDist < 1000)
                sDistType = "m";
            else {
                fDist /= 1000;
                sDistType = "km";
            }
            double fDistOut = (double) fDist;
            fDistOut= Math.round(fDistOut*100)/100D;

            String sDist = String.format("%.1f %s", fDistOut, sDistType);

            DataLine Dat = new DataLine(sMovingType, sDist, sTime, PlaceStatus.PLACE_MOVING, type);
            m_DataList.add(Dat);

//            Log.d(TAG, "Travelling for: " + fDistOut + sDistType + ",  " + TimeDiff + " Minutes...");
        }

        private long TimeDiff(String sCurr, String sLast)
        {
            if (sLast == null)
                return 0;

            long nRetVal = 0;
            try {
                Date dCurr = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(sCurr);
                Date dLast = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(sLast);
                long timeDiff = dCurr.getTime() - dLast.getTime();
                if (timeDiff < 0)
                    timeDiff *= -1;
                nRetVal = ((timeDiff / (1000 * 60)));
            } catch(Exception e) {
                Log.e(TAG, "Error parsing: " + e.getMessage());
            }
            return nRetVal;
        }

        private float DistanceBetween(LatLng posCur, LatLng posLast)
        {
            if (posLast == null)
                return 0;
            Location locationA = new Location("point A");

            locationA.setLatitude(posCur.latitude);
            locationA.setLongitude(posCur.longitude);

            Location locationB = new Location("point B");

            locationB.setLatitude(posLast.latitude);
            locationB.setLongitude(posLast.longitude);
            return locationA.distanceTo(locationB);
        }

        private String GetString(int Id)
        {
            return m_ThisWIndow.getContext().getResources().getString(Id);
        }

        private void GetPhotosForDate()
        {
            LocalDate DateTo = m_Date.plusDays(1);
            long lto = getTimeStamp(DateTo);
            long lfrom = getTimeStamp(m_Date);

            try {
                m_PhotoList = new ArrayList<String>();
                final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE};//get all columns of type images
                Cursor cursor = m_ThisWIndow.getContext().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, //new String[]{MediaStore.Images.Media.DATA},
                        MediaStore.Images.Media.DATE_TAKEN + "> ? AND " + MediaStore.Images.Media.DATE_TAKEN + "<?",
                         new String[]{lfrom+"", lto+ ""}, null);
                Log.d(TAG,"Cursor count: " + cursor.getCount() + " From: " + DateTo + ","+lfrom+" To: "+ m_Date+","+lto);
                while (cursor.moveToNext()) {
                    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    String absolutePathOfImage = cursor.getString(column_index_data);
                    m_PhotoList.add(absolutePathOfImage);
                    Log.e(TAG, "Added photo: " + absolutePathOfImage);
                }
            } catch(Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }

        }

        private long getTimeStamp(LocalDate in_date) {
            String calculatedDate = String.format("%d-%d-%d", in_date.getDayOfMonth(), in_date.getMonthValue(), in_date.getYear());
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date date = null;

            try {
                date = formatter.parse(calculatedDate);
            } catch (Exception e) {
                e.getMessage();
            }

            long output = 0;
            if (date != null) {
                output = date.getTime() / 1000L;
            }
            String str = Long.toString(output);
            return Long.parseLong(str) * 1000;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    private class ListPlacesAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            if (m_DataList == null || m_BuildDataSem.availablePermits() <= 0)
                return 0;

            return m_DataList.size();
        }

        @Override
        public Object getItem(int position)
        {
            if (m_DataList == null || m_BuildDataSem.availablePermits() <= 0)
                return null;
            return m_DataList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {

            DataLine data = m_DataList.get(position);
            if (data == null || m_BuildDataSem.availablePermits() <= 0)
                return convertView;

            if (convertView == null) {
                if (data.m_Status == PlaceStatus.PLACE_STATIONARY)
                    convertView = LayoutInflater.from(m_ThisWIndow.getContext()).inflate(R.layout.places_row, parent, false);
                else
                    convertView = LayoutInflater.from(m_ThisWIndow.getContext()).inflate(R.layout.travel_row, parent, false);
                convertView.setClipToOutline(true);
            }

            //
            // Line 1
            TextView Lin1 = convertView.findViewById(R.id.line1);
            Lin1.setText(data.m_sLine1);

            //
            // Line 2
            TextView Lin2 = convertView.findViewById(R.id.line2);
            Lin2.setText(data.m_sLine2);

            TextView Time = convertView.findViewById(R.id.time);
            Time.setText(data.m_sTime);

            if (data.m_Status == PlaceStatus.PLACE_MOVING) {
                ImageView Img = convertView.findViewById(R.id.travel_icon);
                int Resource = R.drawable.drive;
                switch (data.m_MovType) {
                    case MOV_DRIVE:
                        Resource = R.drawable.drive;
                        break;
                    case MOV_WALK:
                        Resource = R.drawable.walk;
                        break;
                    case MOV_FLIGHT:
                        Resource = R.drawable.flight;
                        break;
                }
                Img.setImageResource(Resource);
            }

            return convertView;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    private class DayListAdapter extends RecyclerView.Adapter<DayListAdapter.DayItemViewHolder>
    {
        private JourneyDayList m_List;
        private int nSelected = 0;

        public DayListAdapter()
        {
            RefreshList();
            ReloadPlaces(0);
        }

        private void RefreshList()
        {
            m_List = new JourneyDayList(m_DR);
            m_List.LoadData();
        }

        private boolean ReloadPlaces(int pos)
        {
            if (!m_BuildDataSem.tryAcquire()) {
                return false;
            }

            nSelected = pos;
            notifyItemChanged(pos);

            Log.d(TAG, "Reloading places: m_List.GetCount(): "+ m_List.GetCount() + ", pos" + pos);
            if (m_List == null || m_List.GetCount() < pos) {
                m_BuildDataSem.release();
                return false;
            }

            JourneyDayList.JourneyDayItem Item = m_List.Get(pos);
            LoadDataInThread InitAsync = new LoadDataInThread(Item.m_Date);
            InitAsync.execute();
            return true;
        }

        @NonNull
        @Override
        public DayItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.places_days, parent, false);
            return new DayItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayItemViewHolder holder, int position)
        {
            JourneyDayList.JourneyDayItem Item = m_List.Get(position);
            if (Item == null)
                return;

            holder.Line1.setText(String.format(getResources().getString(R.string.day_no), Item.m_nDay));

            DayOfWeek nDow = Item.m_Date.getDayOfWeek();
            String sDow = nDow.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            Month nMonth = Item.m_Date.getMonth();
            String sMonth = nMonth.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String sOut = String.format("%s %s %d", sDow, sMonth, Item.m_Date.getDayOfMonth());

            holder.Line2.setText(sOut);

            holder.itemView.setSelected(nSelected == position);
        }

        @Override
        public int getItemCount()
        {
            return m_List.GetCount();
        }

        private class DayItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {
            public TextView Line1, Line2;
            public DayItemViewHolder(@NonNull View itemView)
            {
                super(itemView);
                Line1 = itemView.findViewById(R.id.line1);
                Line2 = itemView.findViewById(R.id.line2);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "Day Item clicked");
                int pos = getAdapterPosition();
                notifyItemChanged(nSelected);
                ReloadPlaces(pos);
            }
        }
    }
}