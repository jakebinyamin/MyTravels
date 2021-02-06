package com.jbsw.mytravels;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.jbsw.utils.Prefs;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private ImageButton m_BtnCancel;
    private Spinner m_BtnGps, m_BtnSortJourney, m_BtnSortJournal;
    private Prefs m_Prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        m_Prefs = new Prefs(this);
        m_BtnCancel = (ImageButton) findViewById(R.id.cancel);
        m_BtnCancel.setOnClickListener(this);

        m_BtnGps = (Spinner) findViewById(R.id.gps);
        m_BtnGps.setOnItemSelectedListener(this);
        int nPos = m_Prefs.GetGpsTracker();
        m_BtnGps.setSelection(nPos);

        m_BtnSortJourney = (Spinner) findViewById(R.id.sort_order_journey);
        m_BtnSortJourney.setOnItemSelectedListener(this);
        nPos = m_Prefs.GetSortOrderJourney();
        m_BtnSortJourney.setSelection(nPos);

        m_BtnSortJournal = (Spinner) findViewById(R.id.sort_order_journal);
        m_BtnSortJournal.setOnItemSelectedListener(this);
        nPos = m_Prefs.GetSortOrderJournal();
        m_BtnSortJournal.setSelection(nPos);
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnCancel)
            finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == m_BtnGps)
        {
            m_Prefs.SetGpsTracker(position);
        }
        if (parent == m_BtnSortJourney)
        {
            m_Prefs.SetSortOrderJourney(position);
        }
        if (parent == m_BtnSortJournal)
        {
            m_Prefs.SetSortOrderJournal(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}