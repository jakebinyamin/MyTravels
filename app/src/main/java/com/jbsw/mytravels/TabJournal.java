package com.jbsw.mytravels;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.jbsw.data.NotesTable;
import com.jbsw.data.TravelMasterTable;

// TODO allow deleting of a journal entry
// TODO support View by day
// TODO Add Photo(s) to a journal entry.
// DONE support different types of data
// TODO deal with a situation when there are no entries



public class TabJournal extends Fragment implements View.OnClickListener
{
    private static final String TAG = "TAGTabJournal";

    View m_ThisWIndow;
    private Button m_BtnCreateEntry;
    private ListView m_List;
    private TravelMasterTable.DataRecord m_DR;
    private TripActivity m_Parent = null;
    private JournalAdapter m_JournalAdapter;

    public TabJournal() {
        Log.d(TAG,"In Constructor");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_ThisWIndow =  inflater.inflate(R.layout.fragment_tab_journal, container, false);
        Log.d(TAG,"In onCreateView");
        return m_ThisWIndow;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        Log.d(TAG,"In onViewCreated for TabJournal");
        m_ThisWIndow = view;
        m_Parent = (TripActivity) getActivity();
        m_BtnCreateEntry = (Button) m_ThisWIndow.findViewById(R.id.btn_add_journal);
        m_BtnCreateEntry.setOnClickListener(this);
        m_List = (ListView) m_ThisWIndow.findViewById(R.id.journal_list);
        m_DR = m_Parent.GetDataRecord();
        m_JournalAdapter = new JournalAdapter(m_ThisWIndow.getContext(), m_DR.Id, m_DR.StartDate);
        m_List.setAdapter(m_JournalAdapter);
        m_List.setOnItemClickListener(new ItemSelectedListener());
        ImageView Img = (ImageView) m_ThisWIndow.findViewById(R.id.image);
        Img.setClipToOutline(true);

        UpdateViews();
    }

    private void UpdateViews()
    {
        boolean bNoRecords = m_JournalAdapter.getCount() <= 0;
        LinearLayout NoRecs = (LinearLayout) m_ThisWIndow.findViewById(R.id.journal_none);
        NoRecs.setVisibility(bNoRecords ? View.VISIBLE : View.GONE );
        m_List.setVisibility(bNoRecords ? View.GONE : View.VISIBLE );
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"In onResume");
        m_JournalAdapter.Refresh();
        UpdateViews();
    }

    private class ItemSelectedListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            NotesTable.DataRecord DR = (NotesTable.DataRecord) m_JournalAdapter.getItem(position);
            Intent intent = new Intent(m_ThisWIndow.getContext(), JournalActivity.class);
            Log.d(TAG, "Journal Id is: " + DR.Id);
            intent.putExtra(JournalActivity.PARAM_MASTER_ID, m_DR.Id);
            intent.putExtra(JournalActivity.PARAM_NODE_ID, DR.Id);
            intent.putExtra(JournalActivity.PARAM_MODE, JournalActivity.ModeEdit);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v == m_BtnCreateEntry)
        {
            Intent intent = new Intent(m_ThisWIndow.getContext(), JournalActivity.class);
            intent.putExtra(JournalActivity.PARAM_MASTER_ID, m_DR.Id);
            intent.putExtra(JournalActivity.PARAM_MODE, JournalActivity.ModeNewRecord);
            startActivity(intent);
        }
    }
}
