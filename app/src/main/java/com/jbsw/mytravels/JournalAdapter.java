package com.jbsw.mytravels;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbsw.data.NotesTable;
import com.jbsw.data.PhotoLinkTable;
import com.jbsw.utils.PhotoBackgroundLoader;
import com.jbsw.utils.Utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

// TODO Add photo to this view
// TODO Change date into somethign nicer eg 12:20 MON 22 Feb 2020

public class JournalAdapter extends BaseAdapter
{
    private static final String TAG = "TAGJournalAdapter";

    private Context m_Context;
    private NotesTable m_NotesTable;
    private long m_Id;
    private String m_StartDate;
    private TabJournal m_TabJournal;
    private PhotoBackgroundLoader m_BkgLoader;

    private static final int NoteDisplayLen = 50;

    public JournalAdapter(TabJournal TJournal, long id, String sStartDate)
    {
        m_TabJournal = TJournal;
        m_Context = TJournal.getContext();
        m_Id = id;
        m_NotesTable = new NotesTable();
        m_StartDate = sStartDate;

        m_BkgLoader = new PhotoBackgroundLoader();
        m_BkgLoader.SetNotLoadedResource(R.drawable.photo_loading_bkg);
        m_BkgLoader.SetRoundedEdge();

        Refresh();
    }

    private class RefreshInThread extends AsyncTask<Object,Void,String>
    {
        @Override
        protected String doInBackground(Object... objects) {
            m_NotesTable.QueryAllForId(m_Id);
            return null;
        }
        @Override
        protected void onPostExecute(String str)
        {
            notifyDataSetChanged();
            m_TabJournal.UpdateViews();
        }
    }

    public void Refresh()
    {
        RefreshInThread Thrd = new RefreshInThread();
        Thrd.execute();
//        m_NotesTable.QueryAllForId(m_Id);
//        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return m_NotesTable.GetRecordCount();
    }

    @Override
    public Object getItem(int position)
    {
        NotesTable.DataRecord DR = m_NotesTable.GetDataAtPosition(position);
        return DR;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null) {
            convertView = LayoutInflater.from(m_Context).inflate(R.layout.journal_record_row, parent, false);
            convertView.setClipToOutline(true);
        }

        NotesTable.DataRecord DR = m_NotesTable.GetDataAtPosition(position);
        if (DR == null)
            return convertView;

        //
        // Get correct icon
        final int IconList[] = Utils.GetIconList();
        ImageView Img = (ImageView) convertView.findViewById(R.id.journal_icon);
        Img.setImageResource(IconList[DR.nType]);

        //
        // Which day of trip
        long nDays = Utils.CalculateDays(m_StartDate, DR.Date);
        TextView Days = (TextView) convertView.findViewById(R.id.day_numb);
        String sDay;
        if (nDays > 0)
            sDay = String.format(parent.getResources().getString(R.string.day_no), nDays);
        else
            sDay = "";
        Days.setText(sDay);

        //
        // Date
        TextView Date = convertView.findViewById(R.id.journal_date);
        Date.setText(Utils.GetReadableStringDateWithTime(DR.Date));

        //
        // Journal Title
        TextView sEntry = convertView.findViewById(R.id.journal_text);
        sEntry.setText(DR.sTitle);

        //
        // Add a photo if there is one.
        PhotoLinkTable TabPhotos = new PhotoLinkTable();
        ImageView PhotoView = convertView.findViewById(R.id.image);
        if (TabPhotos.QueryForNoteEntry(DR.Id) == -1)
        {
            PhotoView.setVisibility(View.GONE);
        }
        else
        {
            PhotoView.setVisibility(View.VISIBLE);
            PhotoLinkTable.DataRecord PhotoDr;
            PhotoDr = TabPhotos.GetNextRecord();
            m_BkgLoader.LoadPhoto(PhotoDr.sPath, PhotoView);
        }

        return convertView;
    }

}
