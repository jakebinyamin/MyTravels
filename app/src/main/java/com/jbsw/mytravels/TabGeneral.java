package com.jbsw.mytravels;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jbsw.data.TravelMasterTable;
import com.jbsw.utils.GpsTracker;
import com.jbsw.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TabGeneral extends Fragment implements View.OnClickListener
{
    private static final String TAG = "TAGTabGeneral";
    private TravelMasterTable.DataRecord m_DR;
    private View m_ThisWIndow;
    private ImageView m_StatusIcon;

    private EditText m_Descr, m_Title;
    private TripActivity m_Parent;
    private Button m_BtnRecord ,m_BtnStop, m_BtnContinue;
    private CheckBox m_GpsCheckBox;

    public TabGeneral() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return  inflater.inflate(R.layout.fragment_tab_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "In onViewCreated");
        m_ThisWIndow = view;
        m_Parent = (TripActivity) getActivity();
        assert m_Parent != null;
        m_DR = m_Parent.GetDataRecord();

        m_BtnRecord = m_ThisWIndow.findViewById(R.id.btn_record);
        m_BtnRecord.setOnClickListener(this);
        m_BtnStop = m_ThisWIndow.findViewById(R.id.btn_stop);
        m_BtnStop.setOnClickListener(this);
        m_BtnContinue = m_ThisWIndow.findViewById(R.id.btn_continue);
        m_BtnContinue.setOnClickListener(this);
        m_StatusIcon = (ImageView) m_ThisWIndow.findViewById(R.id.status_icon);

        m_Descr = view.findViewById(R.id.TabTripDescription);
        if (m_Descr != null && m_DR != null)
            m_Descr.setText(m_DR.Descr);
        m_Title = view.findViewById(R.id.TabTripTitle);
        if (m_Title != null && m_DR != null)
            m_Title.setText(m_DR.Name);

        m_GpsCheckBox = (CheckBox) view.findViewById(R.id.GpsCheckBox);
        if (m_DR != null)
            m_GpsCheckBox.setChecked(m_DR.UseGps);

        SetupViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    private void SetupViews()
    {
        //
        // Setup the correct view to display
        ViewGroup lDates = m_ThisWIndow.findViewById(R.id.mode_dates);
        TextView EndDate = (TextView) m_ThisWIndow.findViewById(R.id.trip_end_date);

        if (m_DR.Status == TravelMasterTable.StatusNotStarted)
        {
            lDates.setVisibility(View.GONE);

            m_BtnStop.setVisibility(View.GONE);
            m_BtnContinue.setVisibility(View.GONE);
            m_BtnRecord.setVisibility(View.VISIBLE);
            m_StatusIcon.setVisibility(View.GONE);
        }
        if (m_DR.Status == TravelMasterTable.StatusInProgress)
        {
            lDates.setVisibility(View.VISIBLE);
            EndDate.setText(R.string.still_not_complete);

            m_BtnStop.setVisibility(View.VISIBLE);
            m_BtnContinue.setVisibility(View.GONE);
            m_BtnRecord.setVisibility(View.GONE);
            m_StatusIcon.setVisibility(View.VISIBLE);
            m_StatusIcon.clearAnimation();
            Utils.SetRecordingAnimation(m_StatusIcon);
        }
        if (m_DR.Status == TravelMasterTable.StatusComplete)
        {
            lDates.setVisibility(View.VISIBLE);
            EndDate.setText(Utils.GetReadableStringDate(m_DR.EndDate));

            m_BtnStop.setVisibility(View.GONE);
            m_BtnContinue.setVisibility(View.VISIBLE);
            m_BtnRecord.setVisibility(View.GONE);
            m_StatusIcon.setVisibility(View.VISIBLE);
            m_StatusIcon.clearAnimation();
            m_StatusIcon.setImageResource(R.drawable.tick);
        }

        //
        // Load the data
        TextView StartDate = (TextView) m_ThisWIndow.findViewById(R.id.trip_start_date);
        if (StartDate!= null)
        {
            String sStartDate = Utils.GetReadableStringDate(m_DR.StartDate);
            StartDate.setText(sStartDate);
        }

        String StatusList[] = getResources().getStringArray(R.array.trip_status_array);
        TextView Status = (TextView) m_ThisWIndow.findViewById(R.id.status_text);
        Status.setText(StatusList[m_DR.Status]);

        String InstructionList[] = getResources().getStringArray(R.array.trip_instructions);
        TextView tStartInstructs = m_ThisWIndow.findViewById(R.id.instructions);
        tStartInstructs.setText(InstructionList[m_DR.Status]);
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnRecord)
        {
            m_DR.Status = TravelMasterTable.StatusInProgress;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            m_DR.StartDate = sdf.format(new Date());
            m_Parent.UpdateData();
            SetupViews();
        }

        if (v== m_BtnStop)
        {
            m_DR.Status = TravelMasterTable.StatusComplete;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            m_DR.EndDate = sdf.format(new Date());
            m_Parent.UpdateData();
            SetupViews();
        }
        if (v== m_BtnContinue)
        {
            m_DR.Status = TravelMasterTable.StatusInProgress;
            m_DR.EndDate = "";
            m_Parent.UpdateData();
            SetupViews();
        }

        CheckForGPSMonitor();
    }

    private void CheckForGPSMonitor()
    {
        GpsTracker gps  = GpsTracker.GetTracker();
        if (gps == null)
            return;

        gps.CheckToStartGPSMonitor();
    }

    public TravelMasterTable.DataRecord GetDataRecord()
    {
        if (m_Descr != null)
            m_DR.Descr = m_Descr.getText().toString();
        if ( m_Title != null)
            m_DR.Name = m_Title.getText().toString();
        m_DR.UseGps = m_GpsCheckBox.isChecked();

        return m_DR;
    }
}
