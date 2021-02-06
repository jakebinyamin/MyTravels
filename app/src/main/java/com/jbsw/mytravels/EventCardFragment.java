package com.jbsw.mytravels;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbsw.data.NotesTable;
import com.jbsw.utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventCardFragment extends Fragment implements View.OnClickListener
{
    private static final String TAG = "TAGEventCardFragment";

    private static final String BUNDLE_DATA = "Data";
    private static NotesTable m_NotesTable;
    private static String m_StartDate;

    private int m_Position;

    public static EventCardFragment newInstance(NotesTable Tab, String startDate, int nPos)
    {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_DATA, nPos);
        m_NotesTable = Tab;
        m_StartDate = startDate;
        EventCardFragment fragment = new EventCardFragment();
        Log.d(TAG, "Creating new Fragment.. " + nPos);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.journal_card, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        m_Position = getArguments().getInt(BUNDLE_DATA);
        NotesTable.DataRecord DR = m_NotesTable.GetDataAtPosition(m_Position);
        Log.d(TAG, "EventCardFragment::onCreate, Pos: "+ m_Position + " Title: " + DR.sTitle);
        view.setOnClickListener(this);
        if (DR == null) {
            Log.e(TAG, "Nothing found!!");
            return;
        }

        final int IconList[] = Utils.GetIconList();
        ImageView Icon = view.findViewById(R.id.journal_icon);
        Icon.setImageResource(IconList[DR.nType]);

        long nDays = Utils.CalculateDays(m_StartDate, DR.Date);
        TextView Days = (TextView) view.findViewById(R.id.day_numb);
        String sDay;
        if (nDays > 0)
            sDay = String.format(view.getResources().getString(R.string.day_no), nDays);
        else
            sDay = "";
        Days.setText(sDay);

        TextView Title = view.findViewById(R.id.journal_text);
        Title.setText(DR.sTitle);

        TextView Date1 = view.findViewById(R.id.journal_date);
        String sOut = Utils.GetReadableStringDate(DR.Date);
        Date1.setText(sOut);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "position requested is:" + m_Position);
        NotesTable.DataRecord DR = m_NotesTable.GetDataAtPosition(m_Position);

        Intent intent = new Intent(getContext(), ViewJournal.class);
        intent.putExtra(JournalActivity.PARAM_MASTER_ID, DR.Id);
        intent.putExtra(JournalActivity.PARAM_MODE, JournalActivity.ModeNewRecord);
        intent.putExtra(JournalActivity.PARAM_NODE_ID, DR.Id);
        startActivity(intent);
    }
}
